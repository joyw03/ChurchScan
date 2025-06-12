import pandas as pd
import re

# CSV 파일 읽기
df = pd.read_csv('jesus114_교단결의.csv')

# 정제 함수 정의
def clean_title(title):
    # 연도/회기 형식 제거 (예: 2018/68, /68, /41 등)
    title = re.sub(r'\b\d{4}/\d+\b', '', title)
    title = re.sub(r'/\d+\b', '', title)
    # 특수문자 제거 (한글, 영어, 숫자 제외)
    title = re.sub(r'[^\w\s가-힣]', '', title)
    # 공백 제거
    title = title.replace(' ', '')
    return title.strip()

# title 컬럼 정제 적용
df['title'] = df['title'].astype(str).apply(clean_title)

# 저장
df.to_csv('jesus114_교단결의_정제본.csv', index=False, encoding='utf-8-sig')

print("✅ 정제 완료! 'jesus114_교단결의_정제본.csv'로 저장됨")
