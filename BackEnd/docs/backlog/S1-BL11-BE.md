# S1-BL11-BE — Daily calories

Kế hoạch: 2026-09-26 → 2026-09-27. Rà soát/phát hành thực tế: 2026-11-13.

Đây là cập nhật/tổng hợp theo backlog từ bản chính đã hoàn thiện, không phải lịch sử phát triển gốc. Giữ ngày commit thực tế. Tính năng đã có trong team repo được giữ lại, không xóa để giả lập làm mới.

Vào UI: Trang chủ → chọn ngày → Calo còn lại.

API /api/v1: `GET /dashboard/daily?date=`.

Kiểm tra: Thêm/sửa/xóa món hoặc vận động: tổng calo cập nhật; tiến độ đã ăn/mục tiêu; còn lại = mục tiêu − đã ăn + vận động.

Bản source cuối được kiểm thử/build; từng checkpoint chỉ được kiểm tra như ghi trong verification.json. Cần thử OAuth/camera/provider/PostgreSQL thật trên máy giáo viên/học sinh.
