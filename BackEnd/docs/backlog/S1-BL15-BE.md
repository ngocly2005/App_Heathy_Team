# S1-BL15-BE — Food camera

Kế hoạch: 2026-10-01 → 2026-10-03. Rà soát/phát hành thực tế: 2026-11-13.

Đây là cập nhật/tổng hợp theo backlog từ bản chính đã hoàn thiện, không phải lịch sử phát triển gốc. Giữ ngày commit thực tế. Tính năng đã có trong team repo được giữ lại, không xóa để giả lập làm mới.

Vào UI: Nút + → Chụp món ăn; hoặc Tài khoản → Chụp món ăn.

API /api/v1: `POST /ai/photo/analyze; POST /ai/photo/confirm`.

Kiểm tra: Cấp quyền camera hoặc chọn ảnh; phân tích chưa ghi nhật ký; sửa kết quả và xác nhận mới ghi. Cần kiểm thử camera/Groq thật.

Bản source cuối được kiểm thử/build; từng checkpoint chỉ được kiểm tra như ghi trong verification.json. Cần thử OAuth/camera/provider/PostgreSQL thật trên máy giáo viên/học sinh.
