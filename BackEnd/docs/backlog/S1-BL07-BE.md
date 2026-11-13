# S1-BL07-BE — Profile BMI/TDEE

Kế hoạch: 2026-09-21 → 2026-09-25. Rà soát/phát hành thực tế: 2026-11-13.

Đây là cập nhật/tổng hợp theo backlog từ bản chính đã hoàn thiện, không phải lịch sử phát triển gốc. Giữ ngày commit thực tế. Tính năng đã có trong team repo được giữ lại, không xóa để giả lập làm mới.

Vào UI: Tài khoản → Hồ sơ thể chất → Thiết lập/Chỉnh sửa.

API /api/v1: `GET/PUT /profile`.

Kiểm tra: Đổi chiều cao/cân nặng/mức vận động, so BMI/TDEE xem trước với BE sau lưu và mở lại.

Bản source cuối được kiểm thử/build; từng checkpoint chỉ được kiểm tra như ghi trong verification.json. Cần thử OAuth/camera/provider/PostgreSQL thật trên máy giáo viên/học sinh.
