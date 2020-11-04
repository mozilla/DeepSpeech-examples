"""Module containing models used for server responses"""

class Response:
    """Response indicating successful operation"""
    def __init__(self, text, time):
        self.text = text
        self.time = time


class ErrorResponse:
    """Response indicating failed operation"""
    def __init__(self, message):
        self.message = message
