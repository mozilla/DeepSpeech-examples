import ast

import pytest
from pyhocon import ConfigFactory
from sanic.websocket import WebSocketProtocol

from deepspeech_server.app import app

# Load app configs and initialize DeepSpeech model
conf = ConfigFactory.parse_file("application.conf")


@pytest.fixture
def sanic_server(loop, sanic_client):
    return loop.run_until_complete(sanic_client(app, protocol=WebSocketProtocol))


def test_index_returns_200():
    request, response = app.test_client.get("/")
    assert response.status == 200


async def test_valid_audio(sanic_server):
    ws_conn = await sanic_server.ws_connect(conf['server.stt_endpoint'])
    with open("tests/experience_proves_this.wav", mode="rb") as file:
        audio = file.read()
        await ws_conn.send_bytes(audio)
        result = await ws_conn.receive()
        assert ast.literal_eval(result.data)["text"] == "experience proves this"


async def test_invalid_audio(sanic_server):
    ws_conn = await sanic_server.ws_connect(conf['server.stt_endpoint'])
    audio = b"this ain't audio"
    await ws_conn.send_bytes(audio)
    result = await ws_conn.receive()
    assert ast.literal_eval(result.data)["message"] == "Something went wrong"
