create database if not exists pri_meeting_java_3;
use  pri_meeting_java_3;


create table if not exists users ( -- thông tin người dùng
    id int primary key auto_increment,
    username varchar(50) unique not null ,
    password varchar(60)not null ,
    full_name varchar(100),
    role enum('ADMIN','EMPLOYEE','SUPPORT') not null ,
    department varchar(100), -- phòng ban
    phone varchar(20)
);


create table if not exists rooms(
    id int  primary key auto_increment,
    room_name varchar(50) not null ,
    capacity int not null, -- sức chứa
    location varchar(100), -- vị trí
    status tinyint default 1 -- 1: hđ,0: bảo trì
);


create table if not exists equipments( -- thiết bị
    id int primary key auto_increment,
    name varchar(100) not null ,
    total_qty int default 0,
    available_qty int default 0 -- sl còn lại

);
create  table if not exists services( -- dịch vụ
    id int primary key auto_increment,
    service_name varchar(100),
    price double

);

create table if not exists bookings(
    id int primary key auto_increment,
    user_id int,
    room_id int,
    start_time datetime not null ,
    end_time datetime not null ,
    status enum('PENDING', 'APPROVED','REJECTED','CANCELLED') default 'PENDING',
    prep_status enum('PREPARING','READY','MISSING') default 'PREPARING',
    support_id int ,


    foreign key (user_id) references users(id),
    foreign key (room_id) references rooms(id),
    foreign key (support_id) references users(id)

);

create table if not exists booking_details(
    id int primary key auto_increment,
    booking_id int,
    equipment_id int null,
    service_id int null,
    quantity int default 1,
    price_at_booking double,


    foreign key (booking_id) references bookings(id),
    foreign key (equipment_id) references equipments(id),
    foreign key (service_id) references  services(id)
);


set foreign_key_checks  = 0;
TRUNCATE TABLE booking_details;
TRUNCATE TABLE bookings;
TRUNCATE TABLE services;
TRUNCATE TABLE equipments;
TRUNCATE TABLE rooms;
TRUNCATE TABLE users;

-- Bat lai kiem tra khoa ngoai
SET FOREIGN_KEY_CHECKS = 1;


insert into users (username, password, full_name, role, department, phone) values
('admin1', '12345678', 'nguyen van admin', 'ADMIN', 'it', '0900000001'),
('emp1', '12345678', 'tran thi a', 'EMPLOYEE', 'hr', '0900000002'),
('emp2', '12345678', 'le van b', 'EMPLOYEE', 'marketing', '0900000003'),
('emp3', '12345678', 'pham thi c', 'EMPLOYEE', 'finance', '0900000004'),
('support1', '12345678', 'do van support', 'SUPPORT', 'it', '0900000005');

insert into rooms (room_name, capacity, location, status) values
('phong hop a', 10, 'tang 1', 1),
('phong hop b', 20, 'tang 2', 1),
('phong hop c', 15, 'tang 3', 1),
('phong hop vip', 5, 'tang 5', 1);

insert into equipments (name, total_qty, available_qty) values
('may chieu', 5, 3),
('laptop', 10, 7),
('micro', 15, 10),
('loa', 6, 4);

insert into services (service_name, price) values
('nuoc uong', 10000),
('banh ngot', 20000),
('tra', 15000),
('coffee', 25000);

insert into bookings (user_id, room_id, start_time, end_time, status, prep_status, support_id) values
(2, 1, '2026-03-27 08:00:00', '2026-03-27 10:00:00', 'APPROVED', 'READY', 5), -- đã duyệt
(3, 2, '2026-03-27 09:00:00', '2026-03-27 11:00:00', 'PENDING', 'PREPARING', 5), -- chờ duyệt
(4, 3, '2026-03-28 13:00:00', '2026-03-28 15:00:00', 'REJECTED', 'MISSING', 5), -- bị từ chối
(2, 4, '2026-03-29 14:00:00', '2026-03-29 16:00:00', 'CANCELLED', 'PREPARING', 5), -- đã hủy
(3, 2, '2026-03-30 08:00:00', '2026-03-30 10:00:00', 'APPROVED', 'READY', 5);

insert into booking_details (booking_id, equipment_id, service_id, quantity, price_at_booking) values

-- booking 1: vừa thiết bị vừa dịch vụ
(1, 1, null, 1, null), -- 1 máy chiếu
(1, 3, null, 2, null), -- 2 micro
(1, null, 1, 10, 10000), -- 10 nước
(1, null, 4, 5, 25000), -- 5 coffee

-- booking 2
(2, 2, null, 1, null), -- 1 laptop
(2, null, 2, 8, 20000), -- 8 bánh

-- booking 3 (thiếu thiết bị)
(3, 1, null, 1, null),
(3, 4, null, 2, null),

-- booking 5
(5, null, 3, 6, 15000), -- trà
(5, 2, null, 2, null); -- laptop



-- Cập nhật mật khẩu chuẩn BCrypt cho admin1 (Pass là: 12345678a)
UPDATE users
SET password = '$2a$10$fYh1CWL1KD/3bbxR6OXdL.PKTP5ymz.qEtIeLxYrnz5Z/K0UZLzs2'
WHERE username = 'admin1';
-- Lệnh SQL cần chạy khi hủy booking:
UPDATE equipments e
JOIN booking_details bd ON e.id = bd.equipment_id
SET e.available_qty = e.available_qty + bd.quantity
WHERE bd.booking_id = ?;