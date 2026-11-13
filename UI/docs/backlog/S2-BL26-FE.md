# S2-BL26-FE — Nutrition macros

Kế hoạch: 2026-10-06 → 2026-10-07. Rà soát/phát hành thực tế: 2026-11-13.

Đây là cập nhật/tổng hợp theo backlog từ bản chính đã hoàn thiện, không phải lịch sử phát triển gốc. Giữ ngày commit thực tế. Tính năng đã có trong team repo được giữ lại, không xóa để giả lập làm mới.

Vào UI: Tài khoản → Dinh dưỡng; hoặc Trang chủ → Tổng quan dinh dưỡng → Chi tiết.

API /api/v1: `GET/PUT /nutrition-settings; GET /dashboard/daily`.

Kiểm tra: Tổng P/C/F và tỷ lệ năng lượng 4/4/9; trường hợp không có món không chia 0; tổng mục tiêu phần trăm phải bằng 100.

Bản source cuối được kiểm thử/build; từng checkpoint chỉ được kiểm tra như ghi trong verification.json. Cần thử OAuth/camera/provider/PostgreSQL thật trên máy giáo viên/học sinh.
