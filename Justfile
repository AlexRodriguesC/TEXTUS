set shell := ["powershell.exe", "-c"]

export PYTHONUTF8 := "1"

test:
    uv run pytest

dev:
    uv run fastapi dev backend/main.py
    uv run python -m huey.bin.huey_consumer backend.worker.huey --verbose

lint:
    uv run ruff check --fix
    uv run ruff format