# AppHealth backend — reconstructed backlog snapshot

Đây là source được phân tách lại từ backend hoàn chỉnh trong `trien_khaicode/`.
Thứ tự thư mục thể hiện backlog đã sắp xếp; không phải bằng chứng thời điểm
thực hiện sprint trong quá khứ. Xem `TASK_STATUS.md` để biết mốc và phần thiếu.

Backend: FastAPI, SQLAlchemy, PostgreSQL, Google sign-in/JWT và Groq từ S1-T03.
Tất cả API nghiệp vụ có prefix `/api/v1`. Swagger: `/docs`; health: `/health`.
Mỗi thư mục backend là source cộng dồn; chỉ dùng một snapshot trong một repo đích.

## Cài và chạy thủ công khi bạn muốn kiểm tra

Chưa có lệnh nào bên dưới được tự chạy khi chia code. Dùng Python 3.12.
Trong root backend đã chọn, tạo venv, cài `requirements.txt`, rồi sao chép
`.env.example` thành `.env`. Cấu hình `GOOGLE_WEB_CLIENT_ID`, JWT secret riêng
và Groq key/model nếu dùng AI. `.env` không thuộc source bàn giao.

```powershell
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
Copy-Item .env.example .env
docker compose up -d postgres
.\.venv\Scripts\python.exe -m alembic upgrade head
.\.venv\Scripts\python.exe -m uvicorn app.main:app --reload
```

Compose riêng dùng database `apphealth_git`, cổng localhost 55432 và volume riêng.
Chỉ dùng database mới cho chuỗi migration `git_*`; không trỏ vào DB gốc của
`trien_khaicode/` hoặc `backend/`. Không chạy `alembic stamp head` lên DB cũ.
Khi chuyển sang snapshot sau: cập nhật dependencies và chạy upgrade đến head mới.
Migration seed catalog giữ lại dữ liệu seed khi downgrade, dùng insert-on-conflict
để có thể upgrade lại; schema không thay đổi ở mốc đó.

Google login cần ID token thật hoặc môi trường kiểm tra có mock; không có endpoint
bỏ qua xác minh. AI cần model phù hợp với request và key của bạn; chưa gọi provider
hay xác nhận khả năng hỗ trợ ảnh của model khi đóng gói. Chat hiện stateless.

Ba task Admin chưa có trong nguồn: S1-T05, S2-T06, S3-T04. Không coi Google user
login, catalog read API hoặc daily dashboard cá nhân là API quản trị.
