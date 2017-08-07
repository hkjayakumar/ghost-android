from typing import Optional

from flask import jsonify

from enum import Enum


class Error(Enum):
    MISSING_ARGS = 1
    ILLEGAL_ARGS = 2


error_messages = {Error.MISSING_ARGS: 'Missing arguments', Error.ILLEGAL_ARGS: 'Illegal arguments'}


def bad_request(err: Error, message: str = None):
    if message is None:
        message = error_messages[err]
    err_obj = {'id': err.value, 'message': message}
    response = jsonify({'error': err_obj})
    response.status_code = 400
    return response
