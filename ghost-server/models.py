import datetime
import enum
from typing import List, Any, Dict

from flask import current_app
from itsdangerous import (TimedJSONWebSignatureSerializer as Serializer, BadSignature, SignatureExpired)
from sqlalchemy import ForeignKey
from sqlalchemy.dialects.postgresql import ARRAY
from sqlalchemy.orm import relationship
from werkzeug.security import generate_password_hash, check_password_hash

from extensions import db
from utils import str_from_date, str_from_date_time


class BaseModel(db.Model):
    __abstract__ = True


class UserModel(BaseModel):
    __tablename__ = 'users'
    id = db.Column(db.Integer, primary_key=True)  # type:int

    # required fields after signup to send messages
    registration_id = db.Column(db.Integer, index=True)  # type:int
    identity_public_key = db.Column(db.String())  # type:str
    signed_pre_key = db.Column(db.String())  # type:str
    one_time_pre_keys = db.Column(ARRAY(db.String())) # type: List[str]

    friends = db.Column(ARRAY(db.Integer())) # type: List[int]


    # LoginModel reference
    login = relationship("LoginModel", uselist=False, back_populates="user")  # type:LoginModel

    def to_dict(self):
        return {'username': self.login.username,
                'registration_id': self.registration_id,
                'identity_public_key': self.identity_public_key,
                'one_time_pre_key': self.one_time_pre_keys,
                'signed_pre_key': self.signed_pre_key}

    def to_public_dict(self):
        one_time_pre_key = self.one_time_pre_keys[0]
        return {'username': self.login.username,
                'registration_id': self.registration_id,
                'identity_public_key': self.identity_public_key,
                'one_time_pre_key': one_time_pre_key,
                'signed_pre_key': self.signed_pre_key}


class LoginModel(BaseModel):
    __tablename__ = 'logins'
    user_id = db.Column(db.Integer, ForeignKey(UserModel.id), primary_key=True)  # type:int
    username = db.Column(db.String(), index=True)  # type:str
    password_hash = db.Column(db.String(128))  # type:str

    user = relationship("UserModel", back_populates="login", uselist=False)  # type: UserModel

    def hash_password(self, password: str):
        self.password_hash = generate_password_hash(password)

    def verify_password(self, password: str) -> bool:
        return check_password_hash(self.password_hash, password)

    def generate_auth_token(self, expiration=600):
        s = Serializer(current_app.config['SECRET_KEY'], expires_in=expiration)
        return s.dumps({'user_id': self.user_id})

    @staticmethod
    def verify_auth_token(token):
        s = Serializer(current_app.config['SECRET_KEY'])
        try:
            data = s.loads(token)
        except SignatureExpired:
            return None  # expired token
        except BadSignature:
            return None  # invalid token
        login = LoginModel.query.get(data['user_id'])  # type: LoginModel
        return login


class MessageModel(BaseModel):
    __tablename__ = 'messages'
    sender_id = db.Column(db.Integer, ForeignKey(UserModel.id), primary_key=True)  # type:int
    receiver_id = db.Column(db.Integer, ForeignKey(UserModel.id), primary_key=True)  # type:int
    message_ciphertext = db.Column(db.String()) # type:str
    timestamp = db.Column(db.DateTime()) # type:datetime.datetime

    def to_dict(self):
        return {'sender_id': self.sender_id,
                'receiver_id': self.receiver_id,
                'message_ciphertext': self.message_ciphertext,
                'description': self.description,
                'timestamp': str_from_date_time(self.timestamp)}