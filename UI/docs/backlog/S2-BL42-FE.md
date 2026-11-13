# S2-BL42-FE — Weight history

Kế hoạch: 2026-10-11 → 2026-10-12. Rà soát/phát hành thực tế: 2026-11-13.

Đây là cập nhật/tổng hợp theo backlog từ bản chính đã hoàn thiện, không phải lịch sử phát triển gốc. Giữ ngày commit thực tế. Tính năng đã có trong team repo được giữ lại, không xóa để giả lập làm mới.

Vào UI: Tài khoản → Cân nặng → chọn tháng/ngày → Lưu/Sửa/Xóa.

API /api/v1: `GET /weight-entries; PUT/DELETE /weight-entries/{date}`.

Kiểm tra: Biểu đồ theo ngày trong tháng; sửa/xóa quá khứ đúng ngày, giữ số lẻ; không đổi số đo mới nhất khi sửa số cũ; chặn tương lai/xóa số đo cuối.

Bản source cuối được kiểm thử/build; từng checkpoint chỉ được kiểm tra như ghi trong verification.json. Cần thử OAuth/camera/provider/PostgreSQL thật trên máy giáo viên/học sinh.
