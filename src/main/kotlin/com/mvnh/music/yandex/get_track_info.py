import yandex_music
import argparse
import sys
import asyncio
import json

async def get_track_info(track_id: str):
    try:
        track = yandex_music.Client().tracks(track_id)[0]
        track_dict = track.to_dict()
        print(json.dumps(track_dict))
    except Exception as e:
        sys.exit(f"Failed to fetch track: {str(e)}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--trackID", help="Yandex Music track ID")
    args = parser.parse_args()
    asyncio.run(get_track_info(args.trackID))