import urllib.request
import json

# 1. 로그인 정보 설정
login_url = "https://mealforyou.store/api/auth/login"
login_data = {
    "email": "test1@gmail.com",
    "password": "test!123"
}

# 2. 로그인 요청 (POST)
try:
    req = urllib.request.Request(
        login_url, 
        data=json.dumps(login_data).encode('utf-8'), 
        headers={'Content-Type': 'application/json'}
    )
    
    with urllib.request.urlopen(req) as response:
        result = json.loads(response.read().decode('utf-8'))
        token = result['accessToken']
        print(f"✅ 로그인 성공! 토큰 획득 완료")

    # 3. 메뉴 상세 조회 요청 (GET)
    dish_url = "https://mealforyou.store/api/dishes/1"
    req_dish = urllib.request.Request(
        dish_url, 
        headers={'Authorization': f'Bearer {token}'}
    )

    with urllib.request.urlopen(req_dish) as response:
        dish_data = json.loads(response.read().decode('utf-8'))
        
        # 4. 결과 출력 (한글 깨짐 방지: ensure_ascii=False)
        print("\n🎉 [조회 결과] ===============================")
        print(json.dumps(dish_data, indent=4, ensure_ascii=False))
        print("==============================================")

except Exception as e:
    print(f"❌ 에러 발생: {e}")