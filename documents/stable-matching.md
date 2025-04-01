# Mô tả bài toán SMT (Stable Matching)
## Giới thiệu về Bài toán Stable Matching

> The Stable Matching Problem (SMP) is a classical problem in algorithmic game theory, introduced by David Gale and Lloyd Shapley in 1962. It involves two sets of agents (e.g., doctors and hospitals, students and schools) who have preferences over each other, and the goal is to create a stable matching, where no two agents would prefer each other over their current partner (Nguồn: Mistralai).

Bài toán này thường được mô tả bằng ví dụ về việc ghép đôi nam và nữ, nhưng nó cũng có thể được áp dụng cho nhiều lĩnh vực khác như ghép đôi sinh viên với các vị trí thực tập, bệnh viện với bác sĩ thực tập, v.v.

### Đầu vào của bài toán

Dữ liệu đầu vào được đóng gói trong `StableMatchingProblemDto` và gửi đến API thông qua `@RequestBody`.

### Các Loại Bài toán Stable Matching

- **One-to-One (1:1) Matching**: Mỗi thực thể chỉ được ghép với một thực thể khác.
- **Many-to-One (M:1) Matching**: Một thực thể có thể được ghép với nhiều thực thể khác.
- **Many-to-Many (M:M) Matching**: Các thực thể có thể được ghép với nhiều thực thể khác.
- **Triplet Matching (TripletOTO)**: Một biến thể mở rộng của Many-to-Many, nơi mỗi cặp ghép bao gồm ba thực thể.

## Implementation

Hệ thống triển khai các thuật toán Stable Matching thông qua các lớp dịch vụ:

- **`StableMatchingService`**: Xử lý yêu cầu chung về bài toán Stable Matching.
- **`StableMatchingOtmService`**: Dành cho bài toán One-to-Many Matching (Không hiểu sao lại tạo riêng cái này =D).
- **`TripletMatchingService`**: *Bản mở rộng của MTM*

Các thuật toán chính được sử dụng:

- **Gale-Shapley Algorithm**: Tạo một cặp ghép đôi ổn định dựa trên danh sách ưu tiên.
- **Gusfield’s Algorithm**: Tối ưu hóa cho các bài toán One-to-One với điều kiện đặc biệt.
- **Farkas’ Algorithm**: Dùng cho bài toán Triplet Matching.

Các thuật toán này được triển khai trong package `ss.smt.implement`, mỗi bài toán có một class tương ứng như `OTMProblem`, `MTMProblem`, `TripletOTOProblem`.

## Cách vận hành của bài toán trong ứng dụng

### DTO

Thư mục `dto/` chứa các lớp dữ liệu truyền tải (DTO) được sử dụng để trao đổi dữ liệu giữa các lớp và dịch vụ. Dữ liệu có thể được gửi từ Frontend dưới dạng JSON hoặc thông qua tệp dữ liệu Excel (`.xlsx`).

- **`StableMatchingProblemDto`**: DTO cho các yêu cầu bài toán Stable Matching.
- **`StableMatchingProblemMapper`**: Chuyển đổi từ DTO sang các mô hình bài toán khác nhau (OTO, OTM, MTM).

### Các thành phần hỗ trợ xử lý bài toán

Thư mục này chứa các classes liên quan đến bài toán Stable Matching:

- **`implement`**: Chứa các lớp thực hiện các loại bài toán Stable Matching như `OTMProblem`, `MTMProblem`, `OTOProblem`, v.v.
- **`evaluator`**: Chứa các lớp đánh giá mức độ phù hợp của các cặp ghép.
- **`preference`**: Chứa các lớp liên quan đến việc xây dựng và quản lý danh sách ưu tiên cho các thực thể.
- **`requirement`**: Chứa các lớp liên quan đến việc xác định các yêu cầu cho việc ghép đôi.

### API và Service trong Java Spring

Các dịch vụ xử lý logic bài toán được triển khai dưới dạng Spring Service:

- **`StableMatchingService`**: Dịch vụ chính để xử lý các yêu cầu liên quan đến bài toán Stable Matching.
- **`StableMatchingOtmService`**: Dịch vụ chuyên biệt cho loại bài toán One-to-Many.

### Ví dụ API Request/Response:

**Lưu ý:** Chưa làm cái này, khi nào mò được tệp JSON thì sẽ sửa sau (Chờ anh hoàng review phần trên nữa).

Các dịch vụ được sử dụng trong Controller để hình thành một API hoàn chỉnh, giúp xử lý bài toán Stable Matching hiệu quả và dễ dàng mở rộng.

