from fastapi import FastAPI
from src.evaluation.api.router import api_router

app = FastAPI(title="TEXTUS Evaluation Service")
app.include_router(api_router)


@app.get("/")
async def root():
    return {"message": "Hello World"}
