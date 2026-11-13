# S2-BL31-BE — Exercise tracking

Kế hoạch: 2026-10-08 → 2026-10-11. Rà soát/phát hành thực tế: 2026-11-13.

Đây là cập nhật/tổng hợp theo backlog từ bản chính đã hoàn thiện, không phải lịch sử phát triển gốc. Giữ ngày commit thực tế. Tính năng đã có trong team repo được giữ lại, không xóa để giả lập làm mới.

Vào UI: Tài khoản → Vận động → Loại vận động + số phút.

API /api/v1: `GET /activity-types; POST /activity-logs; PUT/DELETE /activity-logs/{id}`.

Kiểm tra: Calo MET × 3.5 × kg / 200 × phút; chỉnh thời lượng giữ MET snapshot; ngày/tài khoản độc lập.

Bản source cuối được kiểm thử/build; từng checkpoint chỉ được kiểm tra như ghi trong verification.json. Cần thử OAuth/camera/provider/PostgreSQL thật trên máy giáo viên/học sinh.
