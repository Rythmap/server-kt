# import yandex_music
import argparse
import sys
import asyncio
import requests
# import json

async def get_current_track(token: str):
    # if yandex_token:
    #     try:
    #         yandex_client = yandex_music.Client(yandex_token)
    #     except Exception as e:
    #         sys.exit(f"Failed to initialize Yandex Music client: {str(e)}")
    #     try:
    #         all_queues = yandex_client.queues_list()
    #     except Exception as e:
    #         sys.exit(f"Failed to list queues: {str(e)}")
    #     if all_queues:
    #         try:
    #             latest_queue = yandex_client.queue(all_queues[0].id)
    #             current_track_id = latest_queue.get_current_track()
    #             current_track = current_track_id.fetch_track().to_dict()
    #             print(json.dumps(current_track))
    #         except Exception as e:
    #             sys.exit(f"Failed to fetch track: {str(e)}")
    #     else:
    #         sys.exit("No current tracks")
    # else:
    #     sys.exit("Yandex Music token not provided")
    if token:
        request = requests.get("https://api.mipoh.ru/get_current_track_beta", params={"token": token})
        if request.status_code == 200:
            print(request.json())
        else:
            sys.exit(f"Failed to fetch track: {request.text}")
    else:
        sys.exit("Yandex Music token not provided")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--token", help="Yandex Music token")
    args = parser.parse_args()
    asyncio.run(get_current_track(args.token))