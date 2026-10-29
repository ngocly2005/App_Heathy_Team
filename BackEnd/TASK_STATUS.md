# S1-T03 — Integrate AI Vision model API for food recognition and calorie estimation

Ngày chuẩn bị thực tế: 2026-10-29.
Lịch sử phân tách lại, chưa tạo commit/push và chưa đổi ngày Git.

Backlog owner theo danh sách cung cấp: Ly (không gán Git author).

Trạng thái: **extracted_runtime_unverified**.

Groq adapter, kiểm tra JPEG/PNG/WebP; /ai/photo/analyze và /ai/photo/confirm. Analyze không ghi DB; chỉ confirm mới tạo FoodLog. Không đưa chat vào mốc này.

## Trạng thái cộng dồn

| Task | Trạng thái |
|---|---|
| BASE | extracted_runtime_unverified |
| S1-T01 | extracted_runtime_unverified |
| S1-T02 | extracted_runtime_unverified |
| S1-T03 | extracted_runtime_unverified |

`extracted_runtime_unverified`: có source đã tách, chưa xác nhận chạy API/DB/AI.
`missing_in_source`: chưa có code, chỉ có ghi nhận thiếu; không phải tính năng đã hoàn thành.
