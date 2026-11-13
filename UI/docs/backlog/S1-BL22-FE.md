# S1-BL22-FE — Admin users

Kế hoạch: 2026-09-29 → 2026-10-03. Rà soát/phát hành thực tế: 2026-11-13.

Đây là cập nhật/tổng hợp theo backlog từ bản chính đã hoàn thiện, không phải lịch sử phát triển gốc. Giữ ngày commit thực tế. Tính năng đã có trong team repo được giữ lại, không xóa để giả lập làm mới.

Vào UI: Android: tài khoản Admin → Tài khoản → Mở quản trị → Người dùng; web: chạy admin-web rồi đăng nhập.

API /api/v1: `GET /auth/me; GET /admin/users; PUT /admin/users/{id}`.

Kiểm tra: Teacher cấp role qua admin_setup/DB; thường bị 403, Admin đọc/khóa user; không tự cấp role, không khóa Admin; hai UI dùng chung BE.

Bản source cuối được kiểm thử/build; từng checkpoint chỉ được kiểm tra như ghi trong verification.json. Cần thử OAuth/camera/provider/PostgreSQL thật trên máy giáo viên/học sinh.
