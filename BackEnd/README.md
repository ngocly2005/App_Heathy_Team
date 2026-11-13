# AppHealth backend — bản cập nhật chia task 13/11/2026

Source runtime cuối khớp app/ và tests/ của code_chinh/App_Heathy_BE.
Giữ nguyên migration git_* đã phát hành; revision mới chỉ nối tiếp, không sửa
revision cũ. Dùng database apphealth_git riêng (compose.yaml, cổng 55432).
Không dùng migration code_chinh hoặc stamp revision để trộn hai bộ database.

Tạo/kích hoạt venv, pip install -r requirements.txt; sao chép .env.example thành
.env khi chưa có và cấu hình khóa riêng. Giáo viên sao lưu, kiểm tra alembic current
và alembic heads rồi tự chạy alembic upgrade head; Codex chưa chạy DB thật.
Chạy uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload.

Email/Google dùng chung JWT, role, dữ liệu. Đăng ký email hoặc Google trên Android
rồi giáo viên cấp quyền bằng python -m app.admin_setup --email "email-thuc-te".
Không có Admin mặc định; không lưu mật khẩu plaintext. OAuth/Groq cần cấu hình thật.

Xem docs/backlog/ cho từng task và ../SPRINTS_20261113.md ở root team repo.
Các ngày sprint là kế hoạch; commit dùng ngày tạo thật. Đây là bộ cập nhật được
tách lại với hỗ trợ Codex, không phải lịch sử phát triển gốc của học sinh.
