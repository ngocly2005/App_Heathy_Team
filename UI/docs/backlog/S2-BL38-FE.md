# S2-BL38-FE — Saved meals

Kế hoạch: 2026-10-09 → 2026-10-10. Rà soát/phát hành thực tế: 2026-11-13.

Đây là cập nhật/tổng hợp theo backlog từ bản chính đã hoàn thiện, không phải lịch sử phát triển gốc. Giữ ngày commit thực tế. Tính năng đã có trong team repo được giữ lại, không xóa để giả lập làm mới.

Vào UI: Thực đơn → chọn món từ nhật ký → đặt tên → Lưu bữa ăn; Thêm nhanh.

API /api/v1: `GET/POST/PUT/DELETE /saved-meals; POST /saved-meals/{id}/log`.

Kiểm tra: Lưu bữa, chỉnh/xóa, thêm nhanh đúng ngày/bữa/giờ; xóa thực đơn không xóa nhật ký đã ghi; giữ snapshot món.

Bản source cuối được kiểm thử/build; từng checkpoint chỉ được kiểm tra như ghi trong verification.json. Cần thử OAuth/camera/provider/PostgreSQL thật trên máy giáo viên/học sinh.
