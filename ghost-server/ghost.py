from app import factory

import os


ghost = factory(os.environ['APP_SETTINGS'])
