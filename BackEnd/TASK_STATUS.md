# S1-T02 — Create daily summary aggregation API endpoint

Ngày chuẩn bị thực tế: 2026-10-29.
Lịch sử phân tách lại, chưa tạo commit/push và chưa đổi ngày Git.

Backlog owner theo danh sách cung cấp: Ly (không gán Git author).

Trạng thái: **extracted_runtime_unverified**.

GET /dashboard/daily. Foods/FoodLog DDL là phụ thuộc sớm; chưa có catalog/manual-log API. Calories burned và water là 0 trước task vận động/nước; chưa có breakdown macro.

## Trạng thái cộng dồn

| Task | Trạng thái |
|---|---|
| BASE | extracted_runtime_unverified |
| S1-T01 | extracted_runtime_unverified |
| S1-T02 | extracted_runtime_unverified |

`extracted_runtime_unverified`: có source đã tách, chưa xác nhận chạy API/DB/AI.
`missing_in_source`: chưa có code, chỉ có ghi nhận thiếu; không phải tính năng đã hoàn thành.
