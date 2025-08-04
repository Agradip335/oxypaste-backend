import requests
import random
import time
import uuid

API_URL = "http://localhost:8080/api/pastes"
NUM_PASTES = 25
AUTH_TOKEN = None  # Optional: e.g., "Bearer eyJhbGciOi..."

# Sample structured data with associated languages
PASTE_TEMPLATES = [
    {
        "title": "FizzBuzz in Python",
        "language": "python",
        "content": '''for i in range(1, 101):
    if i % 15 == 0:
        print("FizzBuzz")
    elif i % 3 == 0:
        print("Fizz")
    elif i % 5 == 0:
        print("Buzz")
    else:
        print(i)
'''
    },
    {
        "title": "Simple Express Server",
        "language": "javascript",
        "content": '''const express = require("express");
const app = express();

app.get("/", (req, res) => {
  res.send("Hello from Express!");
});

app.listen(3000, () => {
  console.log("Server running on port 3000");
});
'''
    },
    {
        "title": "Bash: CPU Usage Monitor",
        "language": "bash",
        "content": '''#!/bin/bash
while true; do
  top -b -n1 | grep "Cpu(s)"
  sleep 5
done
'''
    },
    {
        "title": "SQL: Get Active Users",
        "language": "plaintext",  # Assuming SQL is treated as plaintext
        "content": '''SELECT id, username, email
FROM users
WHERE status = 'active'
ORDER BY created_at DESC
LIMIT 50;
'''
    },
    {
        "title": "Java: Reverse a String",
        "language": "java",
        "content": '''public class ReverseString {
    public static void main(String[] args) {
        String input = "OpenAI";
        StringBuilder reversed = new StringBuilder(input).reverse();
        System.out.println(reversed);
    }
}
'''
    },
    {
        "title": "Python: JSON Pretty Printer",
        "language": "python",
        "content": '''import json

data = {
    "name": "John Doe",
    "email": "john@example.com",
    "roles": ["admin", "user"]
}

print(json.dumps(data, indent=4))
'''
    },
    {
        "title": "Next.js Page Example",
        "language": "typescript",  # Next.js commonly uses TS
        "content": '''import React from "react";

export default function Home() {
  return (
    <div className="p-4">
      <h1 className="text-2xl font-bold">Welcome to Next.js</h1>
    </div>
  );
}
'''
    },
    {
        "title": "Dockerfile: Node.js App",
        "language": "plaintext",  # Dockerfile treated as plaintext
        "content": '''FROM node:18

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .

EXPOSE 3000
CMD ["npm", "start"]
'''
    }
]

def generate_paste():
    paste = random.choice(PASTE_TEMPLATES)
    title = paste["title"]
    content = paste["content"]
    language = paste["language"]

    # Add random UUID tag to some titles
    if random.random() < 0.3:
        title += f" ({uuid.uuid4().hex[:4]})"

    return {
        "title": title,
        "content": content,
        "public": True,
        "language": language
    }

def send_paste(paste):
    headers = {
        "Content-Type": "application/json"
    }
    if AUTH_TOKEN:
        headers["Authorization"] = AUTH_TOKEN

    response = requests.post(API_URL, json=paste, headers=headers)
    if response.status_code == 200:
        print(f"✅ Created: {paste['title']}")
    else:
        print(f"❌ Error [{response.status_code}]: {paste['title']}")
        print(response.text)

def main():
    for _ in range(NUM_PASTES):
        paste = generate_paste()
        send_paste(paste)
        time.sleep(0.15)

if __name__ == "__main__":
    main()
