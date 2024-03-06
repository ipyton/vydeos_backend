import csv
import random
import xml.etree.ElementTree as ET

from cassandra.auth import PlainTextAuthProvider
from cassandra.cluster import Cluster
from random import randrange
import datetime

tree = ET.parse("smsCorpus_en_2015.03.09_all.xml")
root = tree.getroot()
# children = root.get("message")
user_list = ["70d60b12-f792-4a76-9940-29f94d3bc850",
             "63e47d0e-49a7-40ec-b12e-cc876084db10",
             "d6f70097-114a-4eab-9d95-4a536746d576",
             "74da7257-2995-4a84-9ccc-1349971be841",
             "a429e17f-1c3e-43ad-829f-2ff5dae5bcae",
             "34afb41d-1d13-4abc-8327-77c58f645549",
             "84f347db-4bbd-4d8c-8ee0-9ac935f4e1a0",
             "769490cd-6b92-40b7-96db-def198b72677",
             "b5c066af-46af-4a89-83f6-7b2eae1f629a",
             "84327312-8a06-4e57-8f9b-bcd59c4af151",
             "46c9d2ea-e816-4eae-88a0-b9ff01c0d39a",
             "442092bf-b7f3-42a0-8b1d-77faf50dc06d",
             "f7559dd5-2e3a-4322-abba-6f20104cd4ff",
             "ec15394d-d795-404f-a248-3e3bf575b9fe",
             "33ff417f-48fd-4b9d-88e6-c931cb69f425",
             "06260e63-364c-4382-bf99-655563b744e3",
             "0da3d92a-7f6f-4555-b7a5-42ef65eb2701",
             "fc392b11-53fc-42a0-aab9-d3965db0d6d5",
             "1d0fc756-606a-4032-954c-a4b615389277",
             "7aa72900-c7d7-4e0e-82c4-86d15738b394",
             "d8b47b8e-8462-4819-9db8-7dc79c1db5fc",
             "20750dbc-8d0c-4bc9-9674-5568be8ba9ea",
             "32cdf4d6-8674-4441-920b-a94763130685",
             "4311e7b1-ceed-4543-88de-57cc7f8f49a7",
             "6de2ff78-2aaa-4b32-b85c-39e77b085cea",
             "58da8d8b-aee3-4a89-9677-c11aa92d750d",
             "1a9d8879-369c-4f1d-aac6-f6564f5fb5d8",
             "d447d1dd-9190-44e7-8c53-bae285fdc34b",
             "551e540b-157c-4d5c-bbf5-5587933aa8ec","0a96f651-f7c9-47ac-b20d-d6b32d082636",
             "54a1f339-26b2-47cd-bda5-2a36847b153a","c844156a-19ca-4daa-bd30-72aadd69abfb",
             "2960788f-cb34-4c2f-b07c-ff3dbd0226bc","31c90799-4b1d-436d-88e6-9ecb59392f3c",
             "a7b296a9-dca1-4805-9652-3ef2b4394108","6359b52c-ce82-464d-840c-842b1d67aea5",
             "953a5189-d301-4c54-a5a7-6dc75f7694da","7fafffaa-b024-408f-bc55-1fbea43acf1e",
             "272d0e6c-6820-4447-bd24-17555669e022","77b762ff-b7d2-4e3d-b3be-f5035f9f369d",
             "bbfb1548-39c8-4347-902e-e889e5caa68e","10602ded-a94b-45bd-9506-2ab629b7c4d1",
             "d3e989f5-8cff-4336-8f10-7f17ca6e190e","b2cbad88-ae7e-40d8-8bf0-4dccdac9effb",
             "1236cb73-f2a8-4ca5-847b-c03d8e142a73","2781cb10-a95a-4ed6-86f6-892d61f426ff"]


startDate = datetime.datetime(2013, 9, 20,13,00)

auth_provider = PlainTextAuthProvider(username='cassandra', password='cassandra')

cluster = Cluster(["192.168.23.129"], auth_provider=auth_provider)
session = cluster.connect()
session.set_keyspace('chat')

for message in root.findall("./message"):
    content = message.find("./text").text
    id = message.attrib['id']
    receicer = user_list[random.randint(0, len(user_list) - 1)]
    sender = user_list[random.randint(0, len(user_list) - 1)]
    send_time = (startDate + datetime.timedelta(days=randrange(10000), hours=randrange(100), minutes=randrange(60),
                                                      seconds=randrange(60))).strftime("%Y-%m-%d %H:%M:%S")
    print(send_time)

    if sender == receicer:
        continue
    print(sender, receicer, id, content, send_time, " ")
    session.execute("insert into chat_record_by_id (user_id, receiver_id, message_id, content, send_time, object_id)values (%s, %s, %s, %s, %s, %s);", (sender, receicer, id, content,
                                                                                          send_time, " "))



