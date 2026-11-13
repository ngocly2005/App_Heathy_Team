# S2-BL34-FE — Water tracking

Kế hoạch: 2026-10-08 → 2026-10-10. Rà soát/phát hành thực tế: 2026-11-13.

Đây là cập nhật/tổng hợp theo backlog từ bản chính đã hoàn thiện, không phải lịch sử phát triển gốc. Giữ ngày commit thực tế. Tính năng đã có trong team repo được giữ lại, không xóa để giả lập làm mới.

Vào UI: Trang chủ → Uống nước ±; Tài khoản → Uống nước.

API /api/v1: `GET/PUT /water-settings; POST /water-logs/adjust; PUT/DELETE /water-logs/{id}`.

Kiểm tra: Chọn ly 200/500 ml, đổi mục tiêu, thêm/bớt không âm; nhật ký và Trang chủ cùng tổng nước.

Bản source cuối được kiểm thử/build; từng checkpoint chỉ được kiểm tra như ghi trong verification.json. Cần thử OAuth/camera/provider/PostgreSQL thật trên máy giáo viên/học sinh.
