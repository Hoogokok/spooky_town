-- :name health-check :? :1
-- :doc 데이터베이스 헬스 체크를 위한 쿼리
SELECT health_check() as status; 