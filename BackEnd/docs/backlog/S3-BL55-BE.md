# S3-BL55-BE — Unified diary

Kế hoạch: 2026-10-30 → 2026-11-03. Rà soát/phát hành thực tế: 2026-11-13.

Đây là cập nhật/tổng hợp theo backlog từ bản chính đã hoàn thiện, không phải lịch sử phát triển gốc. Giữ ngày commit thực tế. Tính năng đã có trong team repo được giữ lại, không xóa để giả lập làm mới.

Vào UI: Tab Nhật ký → chọn ngày → Món ăn/Vận động/Nước/Ghi chú.

API /api/v1: `GET /history/daily?date=; GET/PUT/DELETE /diary/{date}; PUT/DELETE log endpoints`.

Kiểm tra: Sửa/xóa mỗi loại log, ghi chú/tâm trạng đúng ngày; dashboard và lịch cập nhật sau lưu; không cần tải lại thủ công khi chuyển màn.

Bản source cuối được kiểm thử/build; từng checkpoint chỉ được kiểm tra như ghi trong verification.json. Cần thử OAuth/camera/provider/PostgreSQL thật trên máy giáo viên/học sinh.
