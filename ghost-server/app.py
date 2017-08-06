import os

from flask import Flask
from api.user_api import user_api
from api.message_api import message_api

import config
from extensions import db


def factory(configuration=config.DevelopmentConfig):
    application = Flask(__name__)
    application.config.from_object(configuration)
    db.init_app(application)
    application.register_blueprint(user_api)
    application.register_blueprint(message_api)
    
    @application.route('/api/v1/')
    def landing_page():
        return 'Welcome to Ghost API v1'

    return application


if __name__ == '__main__':
    app = factory(os.environ['APP_SETTINGS'])
    app.run()
