# S2-T05 — Create WeightLogs database schema and history tracking API

Ngày chuẩn bị thực tế: 2026-10-29.
Lịch sử phân tách lại, chưa tạo commit/push và chưa đổi ngày Git.

Backlog owner theo danh sách cung cấp: Ly (không gán Git author).

Trạng thái: **extracted_runtime_unverified**.

Model hiện tại tên WeightEntry/table weight_entries; list theo khoảng ngày, upsert/delete. Khôi phục ghi weight history từ profile; migration backfill profile hiện có.

## Trạng thái cộng dồn

| Task | Trạng thái |
|---|---|
| BASE | extracted_runtime_unverified |
| S1-T01 | extracted_runtime_unverified |
| S1-T02 | extracted_runtime_unverified |
| S1-T03 | extracted_runtime_unverified |
| S1-T04 | extracted_runtime_unverified |
| S1-T05 | missing_in_source |
| S2-T01 | extracted_runtime_unverified |
| S2-T02 | extracted_runtime_unverified |
| S2-T03 | extracted_runtime_unverified |
| S2-T04 | extracted_runtime_unverified |
| S2-T05 | extracted_runtime_unverified |

`extracted_runtime_unverified`: có source đã tách, chưa xác nhận chạy API/DB/AI.
`missing_in_source`: chưa có code, chỉ có ghi nhận thiếu; không phải tính năng đã hoàn thành.
