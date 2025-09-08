-- 비밀번호는 애플리케이션에서 인코딩되므로, 여기서는 임시로 그냥 넣지 않고
-- 이미 존재하는 accounts에 대해 UPDATE로 role만 올리는 방식을 권장합니다.
-- 예: 특정 이메일을 관리자 승격
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';