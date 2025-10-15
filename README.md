# Mail Server UDP với Password Authentication

Ứng dụng Mail Server UDP đã được cập nhật với tính năng xác thực mật khẩu.

## Tính năng mới

### 1. **Đăng ký tài khoản với mật khẩu**
- Người dùng cần cung cấp cả username và password khi đăng ký
- Mật khẩu được lưu trữ trong file `MailServerData/users.properties`

### 2. **Đăng nhập**
- Tính năng đăng nhập để xác thực người dùng
- Server phản hồi `LOGIN_SUCCESS` hoặc `LOGIN_FAILED`

### 3. **Gửi email có xác thực**
- Khi gửi email, người dùng phải cung cấp username và password
- Server sẽ xác thực trước khi lưu email
- Server phản hồi `SEND_SUCCESS`, `AUTH_FAILED`, hoặc các lỗi khác

## Cách sử dụng

### Khởi chạy Server
```bash
cd bin
java server.MailServer
```

### Khởi chạy Client
```bash
cd bin
java client.MailClient
```

### Menu Client
1. **Register Account** - Đăng ký tài khoản mới với username và password
2. **Login** - Đăng nhập để kiểm tra thông tin xác thực
3. **Send Email** - Gửi email (yêu cầu xác thực)

## Cấu trúc thư mục

```
MailServerUDP/
├── src/
│   ├── server/MailServer.java    # Server với password authentication
│   ├── client/MailClient.java    # Client với menu mở rộng
│   └── module-info.java
├── bin/                          # Compiled classes
├── MailServerData/
│   ├── users.properties          # File lưu trữ user/password (mới)
│   └── [username]/               # Thư mục email của từng user
└── README.md                     # File này
```

## Protocol Messages

### Đăng ký
- **Client gửi**: `REGISTER:username:password`
- **Server log**: Account created hoặc already exists

### Đăng nhập
- **Client gửi**: `LOGIN:username:password`
- **Server phản hồi**: `LOGIN_SUCCESS` hoặc `LOGIN_FAILED`

### Gửi email
- **Client gửi**: `SEND:sender:password:receiver:subject:content`
- **Server phản hồi**: `SEND_SUCCESS` hoặc `AUTH_FAILED`

## Lưu ý bảo mật

- Mật khẩu được lưu trữ dưới dạng plain text trong file properties
- Để tăng cường bảo mật, có thể implement hash password trong tương lai
- Network communication chưa được mã hóa

## Cải tiến trong tương lai

- [ ] Hash password với salt
- [ ] Mã hóa network communication
- [ ] Session management
- [ ] Password complexity requirements
- [ ] Account lockout after failed attempts