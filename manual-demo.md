# **5\. Tạo Biểu mẫu Dữ liệu**
**[Video hướng dẫn](https://drive.google.com/drive/folders/16e0t3Vhkr4Bc85k0Bd_aFFTEpU6s_s9R?usp=drive_link)**

### **Mục đích của Biểu mẫu Dữ liệu**

Biểu mẫu Dữ liệu cho phép người dùng xác định và cấu trúc dữ liệu đầu vào để giải quyết các bài toán khác nhau một cách hiệu quả. Có hai loại biểu mẫu dữ liệu: biểu mẫu dữ liệu SMT và biểu mẫu dữ liệu GT. Mỗi loại có các yêu cầu riêng về định dạng và cấu trúc đầu vào để đảm bảo giải quyết bài toán chính xác và hiệu quả.

## 5.1. **Lý thuyết Ghép cặp ổn định**

Biểu mẫu dữ liệu yêu cầu người dùng nhập một danh sách những người tham gia cùng với sở thích được xếp hạng của họ. Mỗi người tham gia phải có một danh sách sở thích đầy đủ và được sắp xếp chính xác để duy trì tính nhất quán và công bằng trong quá trình ghép cặp.

#### 5.1.1. **Đặt tên trang tính **

- Bao gồm: "Problem Info", "Problem Data".
- Lưu ý: Nhớ đặt tên trang tính cho đúng, không là lỗi đấy.

#### 5.1.2. **Problem information**

| Tên                          | Giải thích                                                                               | Kiểu dữ liệu | Ghi chú                          |
| ----------------------------- | ----------------------------------------------------------------------------------------- | --------- | ------------------------------ |
| **Problem name**              | Tên được lấy từ dữ liệu do người dùng nhập.                                      | text      |                                |
| **Number of set**             | Tổng số tập hợp tham gia.                            | int       | phải >= 2                   |
| **Number of individuals**     | Tổng số cá nhân trong mỗi tập hợp tham gia.                                | int       | phải >3                     |
| **Number of characteristics** | Tổng số thuộc tính của các cá nhân tham gia.                          | int       |                                |
| **Fitness function**          | Một hàm đánh giá sự phù hợp hoặc hiệu quả của một cá nhân trong mô hình. | text      | Công thức chính xác hoặc giá trị mặc định |
| **Evaluate Function Set_1**   | Tập hợp các hàm đánh giá đầu tiên được sử dụng để đánh giá các cá nhân.                         | text      | Công thức chính xác hoặc giá trị mặc định |
| **Evaluate Function Set_2**   | Tập hợp các hàm đánh giá thứ hai, có thể sử dụng các tiêu chí khác nhau.               | text      | Công thức chính xác hoặc giá trị mặc định |


**Ví dụ**
![](img.jpg)
#### 5.1.3. **Dữ liệu bài toán**

**Các thành phần**

![](img.jpg)

**Cách nhập giá trị?**

Sự ưa thích của Individual A đối với Individual B dựa trên 3 yếu tố:
- **Requirement**
- **Value( Properties)**
- **Weight**

Các yếu tố này có trong mọi thuộc tính của một cá nhân và được hàm Đánh giá (Evaluate function) sử dụng để tính toán sở thích.
| Tên                          | Giải thích                                                                               | Kiểu dữ liệu | Ghi chú                          |
| ----------------------------- | ----------------------------------------------------------------------------------------- | --------- | ------------------------------ |
| **Set_1** | Tên tập hợp hoặc nhóm (set) chứa các cá thể. | text | Ví dụ: "Set_1", "Set_2" |
| **Capacity** | Dung lượng tối đa của từng đối tượng. | int | Phải > 0 |
| **Individual_1** | Tên một participant trong set. | text | Cần điền nếu Capacity > 0 |
| **Requirements, Weights, Properties** | Là các thuộc tính (req, weights, value) của thuật toán Gale-Shapley. | | **KHÔNG ĐƯỢC THAY ĐỔI DỮ LIỆU NÀY** |
| **req_1 ... req_k** | Giá trị của requirement. | text / logical expression | |
| **w_1 ... w_k** | Giá trị của weight. | int | Phải >=0. |
| **p_1 ... p_k** | Giá trị của value( property). | double | Phải >=0. |

---

### Ví dụ

| Individual A | Weight | Value | Requirement |
| :----------: | :----: | :---: | :---------: |
|  Property 1  |   10   |  11   |     12      |
|  Property 2  |   7    |  13   |     13      |
|  Property 3  |   5    |  18   |   18:100    |

| Individual B | Weight | Value | Requirement |
| :----------: | :----: | :---: | :---------: |
|  Property 1  |   3    |  12   |     12      |
|  Property 2  |   8    |  13   |     13      |
|  Property 3  |   6    |  18   |   18:100    |

### Quy tắc cho các thuộc tính

- Weight $>=$ 0
- Value $>=$  0
- Yêu cầu có thể là một số dương (dùng trong các phép tính tùy chỉnh) hoặc một chuỗi (như "1 bound", "2 bounds", hoặc "scale target") (dùng trong các phép tính mặc định).

### Ký hiệu chung

- $R_i$ đại diện the Requirement cho thuộc tính $i$
- $W_i$ đại diện the Weight cho thuộc tính $i$
- $P_i$ đại diện the Value cho thuộc tính $i$

Example: Custom function in Individual A: ($P_1$ - $R_1$) ^ $W_1$

$P_1$ là the Value của thuộc tính 1: 11  
$R_1$ là the Requirement của thuộc tính 1: 12  
$W_1$ là the Weight của thuộc tính 1 :10

Kết quả là: ($P_1$ - $R_1$) ^ $W_1$ = ($11$-$10$) ^ $12$ = $1$

---

### Đánh giá tùy chỉnh

Một hàm đánh giá tùy chỉnh là:

Các giá trị của $R_2$ và $W_1$ được lấy từ Individual A, và $P_1$ được lấy từ Individual B.

- $W_1$ và $P_1$ thì dễ hiểu, còn $R_2$ có chút lằng nhằng.
- Các quy tắc cú pháp cho kết quả khác nhau cho các phép tính Yêu cầu:

1. $x--$ trả về  $x$
2. $x:y$ trả về $\frac{x + y}{2}$

3. $x++$ trả về $x$
4. $x$ trả về $x$

Sử dụng các quy tắc này, sở thích của A đối với B được tính như sau:
$12 + 11 * 12 = 144$

---

### Đánh giá mặc định

Đánh giá mặc định sử dụng tất cả các thuộc tính để tính toán sở thích. Công thức có thể được biểu diễn là:

$$P_A(B) = \sum_{i=1}^{N} R_i(P_i) \times W_i$$

Where:

- $N$ là số lượng thuộc tính của một cá nhân
- $R_i$ là hàm yêu cầu cho thuộc tính $i$ của A
- $W_i$ là trọng số cho thuộc tính $i$ của A
- $P_i$ is là giá trị cho thuộc tính $i$ của B

---

### Các hàm Yêu cầu

1.  
$$
    R_i(P_i) = x - - (P_i) =
    \begin{cases}
    2 & \text{if } x = 0 \\
    0 & \text{if } P_i > x \\
    \frac{x + |P_i - x|}{x} & \text{else}
    \end{cases}
    $$

2.  
$$
    R_i(P_i) = x + + (P_i) =
    \begin{cases}
    2 & \text{if } x = 0 \\
    0 & \text{if } P_i < x \\
    \frac{x + |P_i - x|}{x} & \text{else}
    \end{cases}
    $$

3.  
$$
    R_i(P_i) = x : y (P_i) =
    \begin{cases}
    \frac{|y - x|}{2} - \frac{|x + y|}{2} + \frac{|P_i|}{|y - x|} + 1 & \text{if } P_i \in [x, y] \\
    0 & \text{else}
    \end{cases}
    $$

4.  
$$
    R_i(P_i) = x(P_i) =
    \begin{cases}
    0 & \text{if } P_i < 0 \text{ or } P_i > 10 \\
    0 & \text{if } |P_i - x| > 7 \\
    1 & \text{if } |P_i - x| > 5 \\
    \frac{10 - |P_i - x|}{11} & \text{else}
    \end{cases}
    $$

---

### Áp dụng các Quy tắc này

Bây giờ hãy áp dụng các quy tắc này để tính sở thích của A đối với B.
$$
P_A(B) = R_1(P_1) * W_1 + R_2(P_2) * W_2 + R_3(P_3) * W_3
$$

Thay thế các giá trị từ các bảng:

Điều này đơn giản hóa thành:
$$
= \frac{12 + |12 - 12|}{12} * 12 + 0 * 13 + \left( \frac{100 - 18}{2} - \frac{100 + 18}{2} + 1 \right) * 23
$$

Kết quả là:

$$
= 12 + 0 + 23 = 35
$$

**Làm thế nào để nhập cú pháp hàm?**

- Syntax

  - $: i - chỉ số của MatchSet trong "matches"

    - $: giá trị (1 hoặc 2) đại diện cho tập 1 (0) hoặc tập 2 (1)
    - $: S(set) - Tổng tất cả các điểm payoff của "set" được đánh giá bởi tập hợp đối diện
    - $: M(i) - Giá trị về độ hài lòng của một MatchSet cụ thể, ví dụ: M0 (độ hài lòng của Individualsố 0)
      _ Các hàm được hỗ trợ:
      _ #: SIGMA{S1} tính tổng tất cả MatchSet của một tập hợp, ví dụ: SIGMA{S1}

          * Các phép tính toán học được hỗ trợ:
          * Tên:    Cách dùng
          * 1. trị tuyệt đối        : abs(biểu thức)
          * 2. lũy thừa      : (biểu thức)^(biểu thức)
          * 3. sin                 : sin(biểu thức)
          * 4. cos                 : cos(biểu thức)
          * 5. tan                : tan(biểu thức)
          * 6. logarithm     : log(biểu thức)(biểu thức)
          * 7. căn bậc hai: sqrt(biểu thức)

- Lưu ý: Bạn có thể điền "Default" vào hàm.

## 5.2. **Game Theory**

Biểu mẫu dữ liệu bao gồm việc xác định số lượng người chơi, các chiến lược có sẵn của họ và ma trận kết quả tương ứng. Điều này đảm bảo rằng hệ thống có thể tính toán chính xác các chiến lược và kết quả tối ưu dựa trên dữ liệu đầu vào.

#### 5.2.1. **Đặt tên trang tính **

- Bao gồm: : "Problem Info", "Problem Data".
- Lưu ý: Nhớ đặt tên trang tính cho đúng, không là lỗi đấy.

#### 5.2.2. **Thông tin bài toán**

| Tên                                           | Giải thích                                                                                                                       | Kiểu dữ liệu | Ghi chú                          |
| ---------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- | --------- | ------------------------------ |
| **Problem name**                               | Tên được lấy từ dữ liệu do người dùng nhập.                                                                              | text      |                                |
| **Special Player exists**                      | Xác định xem có người chơi đặc biệt hay không (0: Không, 1: Có). Người chơi đặc biệt có thể có vai trò quan trọng hoặc độc nhất trong trò chơi.  | int       | 0 hoặc 1                         |
| **Number of properties of special player**     | Số lượng đặc điểm hoặc thuộc tính của người chơi đặc biệt.                                                                | int       |                                |
| **Number of normal players**                   | Tổng số người chơi thông thường trong trò chơi.                                                                                  | int       |                                |
| **Number of properties of each normal player** | Số lượng thuộc tính cho mỗi người chơi bình thường, có thể liên quan đến chiến lược, tài nguyên hoặc lợi ích.                          | int       |                                |
| **Fitness function**                           | Một hàm đánh giá sự thành công của một người chơi.                                                                                | text      | Công thức chính xác hoặc giá trị mặc định |
| **Player payoff function**                     | Một hàm xác định phần thưởng hoặc lợi ích mà một người chơi nhận được dựa trên chiến lược và hành động của họ.                       | text      | Công thức chính xác hoặc giá trị mặc định |
| **Is maximizing problem**                      | Chỉ định xem bài toán có phải là bài toán tối đa hóa hay không.                                                                          | boolean   | true hoặc false                  |

  **Ví dụ ở đây**  
  ![](img2.jpg)

#### 5.2.3. **Dữ liệu bài toán**

**Các thành phần**

Ví dụ ở đây

![](img3.jpg)


**Cách nhập giá trị?**
| Tên                          | Giải thích                                                                                       | Kiểu dữ liệu | Ghi chú |
| ----------------------------- | ------------------------------------------------------------------------------------------------- | ------------ | ------- |
| **Player_name**               | Tên của người chơi. Nếu bỏ trống sẽ tự động gán là "Player n".                                   | text         | Không bắt buộc |
| **Number_of_strategies**      | Số lượng chiến lược mà người chơi này có.                                                        | int          | Phải ≥ 1 |
| **Payoff_function**           | Hàm payoff tùy chỉnh của người chơi. Nếu bỏ trống thì dùng mặc định (tổng thuộc tính).           | text         | Ví dụ: là công thức dạng p1+p2*0.5 |
| **Strategy_name**             | Tên chiến lược được người chơi đưa ra.                                                           | text         | Ví dụ: "S1", "S2" |
| **Property_1 ... property_n** | Các thuộc tính định lượng của chiến lược, được dùng để tính payoff.                             | double    | Phải ≥ 0 |



- Players:

  - Trò chơi được biểu diễn ở dạng chiến lược tiêu chuẩn (bình thường).
  - Tập hợp người chơi được ký hiệu là  $I = {1, ..., n}$.
  - Mỗi người chơi iinI có một tập hợp hành động được ký hiệu là **$A_{i}$**.
  - •	Một hồ sơ hành động **$a$ $=$ ($ai$, $a_{-i}$)** bao gồm hành động của người chơi i và hành động của những người chơi khác, được ký hiệu là **$a_{-i}$ = ($a{1}$,..., $a{n}$) ∈ $A_{-i}$**.

- Strategies:

  - Một strategy **$s_i$ ∈ $S_i$** cho người chơi **i**, thì một hàm **$s_i$: H $\to$ $A_{i}$**, trong đó không gian chiến lược của **i** bao gồm **$K_{i}$**chiến lược rời rạc: tức là, **$S_i = {s_i^1,s_i^1, ..., s_i^{K_{i}} }$**
  - Hơn nữa, ký hiệu một sự kết hợp chiến lược của  **n** người chơi ngoại trừ **$i$** là **$s_{-i}$ = ($s{1}$,..., $s{n}$)**
  - Tập hợp các hồ sơ chiến lược chung được ký hiệu là **$S$ = $S_{1}$ x ... x $S_{n}$**

  Mỗi người chơi  **i** có một hàm kết quả  **$\pi_{i}$: S $\to$ R**, đại diện cho kết quả khi hồ sơ chiến lược chung được thực hiện.

- Payoff <<payoff>>

  Kết quả được tính bằng hàm kết quả, mặc định là tổng các thuộc tính của một chiến lược hoặc một hàm tùy chỉnh sử dụng cú pháp`=p<column index>[<arithmetic>]`.

  - p<column index> đề cập đến giá trị trong cột thuộc tính tương ứng.
  - Chỉ số bắt đầu từ 1.

  Ví dụ: If Strategy A A có hai thuộc tính với giá trị 188 và 1.2, hàm kết quả mặc định sẽ tính:
  Payoff(Strategy A) = Property 1 + Property 2 = 188 + 1.2 = 189.2

- Fitness <<fitness>>

  Độ thích nghi thường được suy ra từ kết quả. Hàm thích nghi đơn giản nhất có thể chỉ là chính kết quả đó, nhưng nó cũng có thể kết hợp các yếu tố khác:

  - •	Hàm thích nghi đơn giản: fitness = payoff

  - •	Hàm thích nghi tùy chỉnh: fitness = (payoff \* weight_factor) + other_adjustments

  For example: với hệ số trọng số là  1.5, một hàm thích nghi tùy chỉnh sẽ là:  
  fitness(Player 1) = 189.2 \* 1.5 = 283.8

  fitness(Player 2) = 148.7 \* 1.5 = 223.05

- Giải quyết Xung đột

 Quy tắc xung đột: Một người chơi có thể không được phép chọn một chiến lược cụ thể nếu nó xung đột với lựa chọn của người chơi khác, hoặc nó có thể bị phạt.

  Ví dụ: Nếu Người chơi 1 chọn Chiến lược A và Người chơi 2 cũng chọn Chiến lược A, có thể tồn tại một quy tắc xung đột phạt điểm thích nghi của cả hai người chơi.

  Quy tắc xung đột: Nếu cả hai người chơi chọn Chiến lược A, giảm độ thích nghi đi 20%. Vì vậy, kết quả sẽ là:
   player1*fitness = 189.2 * 0.8 = 151.36  
   player2*fitness = 148.7 * 0.8 = 119.0

  **3.3 Cách nhập cú pháp hàm**
  Xem ví dụ này để hiểu rõ hơn

  ![](img4.png)


- Fitness function: Hàm này tính giá trị trung bình của năm tham số tiện ích (u1 đến u5). Đây là một hàm trung bình đơn giản đánh giá độ thích nghi tổng thể của một người chơi bằng cách lấy trung bình của năm yếu tố này.
- Payoff function:
  - Giá trị đầu tiên (p1) được nhân với 5, nghĩa là nó có tác động đáng kể đến kết quả cuối cùng.
  - Giá trị thứ hai (p2) cũng được nhân với 5 nhưng sau đó được chia cho một tổng không đổi (5+5), làm giảm tác động của nó so với p1.
  - Giá trị thứ ba (p3) bị trừ đi, nghĩa là nó làm giảm phần thưởng tổng thể.
- Lưu ý: Bạn có thể điền "Default" vào hai hàm.

# **6\. Solve**

Bộ giải sẽ xử lý dữ liệu đầu vào và áp dụng các thuật toán để tính toán kết quả.

### **6.1 Thực thi từng bước**

1. Chuẩn bị biểu mẫu dữ liệu đầu vào, sau đó tải tệp này lên. 
![](img5.jpg)
2. Cấu hình bộ giải
   - Chọn một thuật toán (Đối với SMT cần chọn loại bài toán).
   - Cấu hình các tham số thuật toán, như: Số lõi phân tán, Kích thước quần thể, Số thế hệ lai ghép, Thời gian thực thi tối ưu hóa tối đa.
3. Nhấp "Solve now" để bắt đầu quá trình và đợi kết quả. 
![](img6.jpg)

### **6.2 Mẹo tối ưu hóa**
- Số hóa dữ liệu cho các characteristics
- Sử dụng các bộ dữ liệu nhỏ hơn để kiểm tra ban đầu.
- Đảm bảo dữ liệu đầu vào đầy đủ, đúng syntax và được định dạng chính xác.

# **7\. Xem Chi tiết Kết quả**

- Tính năng "Get Result Insight" sẽ khả dụng sau khi giải thành công.

### **7.1 Phân tích Kết quả**

![](img7.jpg)

- "Get Result Insight" sẽ chạy 6 thuật toán, mỗi thuật toán thực thi 10 lần.
- Thời gian hoàn thành ước tính = thời gian chạy trung bình của một thuật toán * số lần thực thi mỗi thuật toán * số lượng thuật toán. Do đó: Thời gian chạy của getInsight có thể rất lâu, và bạn không nên đóng tab hoặc tắt máy tính, vì nó sẽ làm mất phiên làm việc.
- Sau khi chạy GetInsight, bạn có thể xuất kết quả và hình ảnh bằng tính năng "Export". 
  -	Xem kết quả trong các bảng điều khiển với biểu đồ, bảng và các công cụ hỗ trợ trực quan khác.
  - Lọc dữ liệu để tập trung vào các thông tin chi tiết cụ thể
  - Ví dụ (GT)
  ![](img7.jpg)
### **7.2 Định dạng Đầu ra**

- **SMT**: Một bảng chứa các cặp đã ghép và các cặp chưa được ghép (nếu có).
- Ví dụ
![](img.jpg)
- **GT**: Một bảng chứa các chiến lược đã sử dụng và điểm số tương ứng của người chơi.
- Ví dụ
![](img.jpg)
