from fastapi import UploadFile


class PDFService:
    def __init__(self):
        self.abc = 123

    async def handle_upload(self, file: UploadFile):
        print(f"Handling file yay {file.size}")
