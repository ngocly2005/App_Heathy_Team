# S2-BL47-BE — Admin food/exercise catalog

Kế hoạch: 2026-10-20 → 2026-10-25. Rà soát/phát hành thực tế: 2026-11-13.

Đây là cập nhật/tổng hợp theo backlog từ bản chính đã hoàn thiện, không phải lịch sử phát triển gốc. Giữ ngày commit thực tế. Tính năng đã có trong team repo được giữ lại, không xóa để giả lập làm mới.

Vào UI: Admin Android hoặc web → Thực phẩm / Bài tập.

API /api/v1: `GET/POST/PUT/DELETE /admin/foods; GET/POST/PUT/DELETE /admin/activities`.

Kiểm tra: Tạo/sửa/ngừng dùng trên một UI rồi tải lại ở UI kia; danh mục thường ẩn món ngừng dùng; không thay snapshot nhật ký cũ.

Bản source cuối được kiểm thử/build; từng checkpoint chỉ được kiểm tra như ghi trong verification.json. Cần thử OAuth/camera/provider/PostgreSQL thật trên máy giáo viên/học sinh.
