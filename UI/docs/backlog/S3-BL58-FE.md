# S3-BL58-FE — Historical calendar

Kế hoạch: 2026-10-30 → 2026-10-31. Rà soát/phát hành thực tế: 2026-11-13.

Đây là cập nhật/tổng hợp theo backlog từ bản chính đã hoàn thiện, không phải lịch sử phát triển gốc. Giữ ngày commit thực tế. Tính năng đã có trong team repo được giữ lại, không xóa để giả lập làm mới.

Vào UI: Tài khoản → Lịch sử ngày → chuyển tháng → chọn ngày quá khứ.

API /api/v1: `GET /history/calendar?month=; GET /history/daily?date=`.

Kiểm tra: Tháng nhuận, ngày trống, ghi chú/cân nặng; chọn ngày mở đúng nhật ký; chặn ngày tương lai.

Bản source cuối được kiểm thử/build; từng checkpoint chỉ được kiểm tra như ghi trong verification.json. Cần thử OAuth/camera/provider/PostgreSQL thật trên máy giáo viên/học sinh.
