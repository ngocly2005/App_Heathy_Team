# S1-BL02-FE — Authentication

Kế hoạch: 2026-09-15 → 2026-09-17. Rà soát/phát hành thực tế: 2026-11-13.

Đây là cập nhật/tổng hợp theo backlog từ bản chính đã hoàn thiện, không phải lịch sử phát triển gốc. Giữ ngày commit thực tế. Tính năng đã có trong team repo được giữ lại, không xóa để giả lập làm mới.

Vào UI: Màn đầu → Đăng ký / Đăng nhập / Tiếp tục với Google.

API /api/v1: `POST /auth/register; POST /auth/login; POST /auth/google; GET /auth/me; POST /auth/refresh; POST /auth/logout; PUT /auth/password; POST /auth/link-google`.

Kiểm tra: Đăng ký email mới, thoát rồi đăng nhập lại; trùng email bị chặn; liên kết Google trong Bảo mật tài khoản giữ nguyên ID và dữ liệu.

Bản source cuối được kiểm thử/build; từng checkpoint chỉ được kiểm tra như ghi trong verification.json. Cần thử OAuth/camera/provider/PostgreSQL thật trên máy giáo viên/học sinh.
