from pathlib import Path


class PDFFileStorage:
    def __init__(self):
        self.base_path = Path("storage") / "books"

    async def save_book_file(self, book_id: str, file) -> Path:
        path = self.base_path / str(book_id) / "original.pdf"
        path.parent.mkdir(parents=True, exist_ok=True)

        with path.open("wb") as buffer:
            while chunk := await file.read(1024 * 1024):
                buffer.write(chunk)

        await file.close()
        return path
