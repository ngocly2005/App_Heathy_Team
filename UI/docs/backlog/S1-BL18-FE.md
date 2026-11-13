# S1-BL18-FE — Food search and portions

Kế hoạch: 2026-09-21 → 2026-09-22. Rà soát/phát hành thực tế: 2026-11-13.

Đây là cập nhật/tổng hợp theo backlog từ bản chính đã hoàn thiện, không phải lịch sử phát triển gốc. Giữ ngày commit thực tế. Tính năng đã có trong team repo được giữ lại, không xóa để giả lập làm mới.

Vào UI: Nút + → Tìm món ăn → chọn món → nhập gram/bữa/giờ.

API /api/v1: `GET /foods?q=; POST /food-logs; PUT/DELETE /food-logs/{id}`.

Kiểm tra: Tìm tên, thay 100 thành 150 g, kiểm tra tỷ lệ dinh dưỡng; lưu đúng ngày và không lẫn tài khoản.

Bản source cuối được kiểm thử/build; từng checkpoint chỉ được kiểm tra như ghi trong verification.json. Cần thử OAuth/camera/provider/PostgreSQL thật trên máy giáo viên/học sinh.
