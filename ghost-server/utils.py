import datetime
from typing import List


def date_from_str(date_str: str) -> datetime.date:
    arr = date_str.split('/')  # type:List[str]
    return datetime.date(int(arr[0]), int(arr[1]), int(arr[2]))


def date_time_from_str(date_str: str) -> datetime.datetime:
    arr = date_str.split(' ')
    date_arr = arr[0].split('/')
    time_arr = arr[1].split(':')
    return datetime.datetime(int(date_arr[0]), int(date_arr[1]), int(date_arr[2]), int(time_arr[0]),
                                          int(time_arr[1]))


def str_from_date(date: datetime.date) -> str:
    if date is None:
        return None
    return date.strftime('%Y/%m/%d')


def str_from_date_time(date: datetime.datetime) -> str:
    if date is None:
        return None
    return date.strftime('%Y/%m/%d %H:%M')
