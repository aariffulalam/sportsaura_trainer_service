#!/usr/bin/env bash
# Adds sample trainers for LOCAL DEVELOPMENT only. Never run against production.
#
# Sports and website text are not created here: they come from the Flyway migrations.
# This script creates trainer accounts in the user service (if they don't exist yet), logs in as
# each one, and saves their trainer profile through this service's API, exactly as the website does.
#
# Needs: the user service on USER_SERVICE_URL and this service on TRAINER_SERVICE_URL, both running.
#
#   bash scripts/seed-dev-data.sh
#
# Safe to run more than once: existing accounts are reused and profiles are updated.

set -euo pipefail

USER_SERVICE_URL="${USER_SERVICE_URL:-http://localhost:8081}"
TRAINER_SERVICE_URL="${TRAINER_SERVICE_URL:-http://localhost:8082}"
PASSWORD="${SEED_PASSWORD:-Sportsaura@123}"

sports_json=$(curl -sf "$TRAINER_SERVICE_URL/api/v1/sports")

sport_id() { # slug -> id
  echo "$sports_json" | grep -o "\"id\":[0-9]*,\"slug\":\"$1\"" | grep -o '[0-9]*' | head -1
}

ids() { # slug slug ... -> [1,2]
  local out=""
  for slug in "$@"; do out="$out${out:+,}$(sport_id "$slug")"; done
  echo "[$out]"
}

json_escape() { printf '%s' "$1" | sed 's/\\/\\\\/g; s/"/\\"/g'; }

# name email phone city years rate headline bio sport-slugs...
trainer() {
  local name=$1 email=$2 phone=$3 city=$4 years=$5 rate=$6 headline=$7 bio=$8
  shift 8

  curl -s -o /dev/null -X POST "$USER_SERVICE_URL/api/v1/auth/register" \
    -H 'Content-Type: application/json' \
    -d "{\"name\":\"$name\",\"email\":\"$email\",\"password\":\"$PASSWORD\",\"phone\":\"$phone\",\"role\":\"TRAINER\"}"

  local token
  token=$(curl -sf -X POST "$USER_SERVICE_URL/api/v1/auth/login" \
    -H 'Content-Type: application/json' \
    -d "{\"email\":\"$email\",\"password\":\"$PASSWORD\"}" |
    sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
  if [ -z "$token" ]; then
    echo "SKIP  $email (could not log in; does it exist with another password?)"
    return
  fi

  local code
  code=$(curl -s -o /dev/null -w '%{http_code}' -X PUT "$TRAINER_SERVICE_URL/api/v1/trainers/me" \
    -H "Authorization: Bearer $token" -H 'Content-Type: application/json' \
    -d "{\"displayName\":\"$name\",\"headline\":\"$(json_escape "$headline")\",\"bio\":\"$(json_escape "$bio")\",\"city\":\"$city\",\"experienceYears\":$years,\"hourlyRateInr\":$rate,\"sportIds\":$(ids "$@")}")
  echo "$code   $name ($city) -> $*"
}

echo "Seeding trainers (HTTP 200 = saved)"
trainer "Rahul Sharma" "rahul.trainer@sportsaura.dev" "+919000000002" "Mumbai" 8 900 \
  "AIFF D-licensed football coach" \
  "I have coached school and club teams in Mumbai for eight years. Sessions focus on first touch, passing under pressure and match fitness." \
  football fitness
trainer "Priya Nair" "priya.trainer@sportsaura.dev" "+919000000003" "Bengaluru" 6 700 \
  "Certified yoga teacher and swim coach" \
  "Hatha and Vinyasa yoga for flexibility and calm, plus swimming lessons for adults who never learned. Morning batches available." \
  yoga swimming
trainer "Vikram Singh" "vikram.trainer@sportsaura.dev" "+919000000004" "Delhi" 12 1200 \
  "Former Ranji player, batting specialist" \
  "Twelve years of coaching at academies in Delhi. One-to-one nets sessions on technique, footwork and playing spin." \
  cricket
trainer "Meera Iyer" "meera.trainer@sportsaura.dev" "+919000000008" "Chennai" 5 800 \
  "State-level tennis and badminton coach" \
  "I teach beginners and juniors preparing for district tournaments. Clear drills, video feedback and a plan for every student." \
  tennis badminton
trainer "Arjun Patel" "arjun.trainer@sportsaura.dev" "+919000000009" "Pune" 4 600 \
  "Basketball skills and strength coach" \
  "Ball handling, shooting form and the conditioning to last four quarters. Weekend group sessions too." \
  basketball fitness
echo "Done. Sample trainer password: $PASSWORD"
