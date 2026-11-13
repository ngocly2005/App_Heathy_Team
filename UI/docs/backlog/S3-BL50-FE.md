# S3-BL50-FE — Meal AI chat

Kế hoạch: 2026-10-27 → 2026-10-29. Rà soát/phát hành thực tế: 2026-11-13.

Đây là cập nhật/tổng hợp theo backlog từ bản chính đã hoàn thiện, không phải lịch sử phát triển gốc. Giữ ngày commit thực tế. Tính năng đã có trong team repo được giữ lại, không xóa để giả lập làm mới.

Vào UI: Tài khoản → Tư vấn bữa ăn.

API /api/v1: `POST /ai/chat; GET /ai/chat/history?before_id=`.

Kiểm tra: Bữa ăn đầy đủ trong từng tin AI và lịch sử cuộn; chip hỏi tiếp chỉ điền ô nhập; gửi mới thêm tin; cần thử provider thật.

Bản source cuối được kiểm thử/build; từng checkpoint chỉ được kiểm tra như ghi trong verification.json. Cần thử OAuth/camera/provider/PostgreSQL thật trên máy giáo viên/học sinh.
