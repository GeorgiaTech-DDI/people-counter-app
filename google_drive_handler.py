#!/usr/bin/env python
### Non-google customizations
import json
import logging
import sys
import argparse
from oauth2client.client import flow_from_clientsecrets
from oauth2client.file import Storage
from oauth2client import tools
from oauth2client.tools import run_flow, argparser
from oauth2client.client import OAuth2WebServerFlow

from googleapiclient.http import MediaFileUpload

import pickle
import os
from google_auth_oauthlib.flow import InstalledAppFlow
from googleapiclient.discovery import build
from google.auth.transport.requests import Request
import io
import argparse
from apiclient.http import MediaIoBaseDownload

OAUTH2_STORAGE = 'oauth2.dat'
OAUTH2_CLIENT_ID = '725213219574-l2rkm3kmcgtd678amvdcn0no9aq811hc.apps.googleusercontent.com'
OAUTH2_CLIENT_SECRET = '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'
CLIENT_SECRET_FILE = "client_secret.json"
API_NAME = "drive"
API_VERSION = "v3"
SCOPES = ["https://www.googleapis.com/auth/drive"]

class GoogleDriveHandler:
    def __init__(self, argv):
        """ A constructor which creates a google drive instance. This involves two steps. The first is authorizing access using the oauth 2.0 protocol. The second is to actually build the drive."""
        print('begin authorization stuff')
        self.drive_service = self.build_drive_service(CLIENT_SECRET_FILE, API_NAME, API_VERSION, SCOPES)
        # self.drive_file_list = self.drive_service.files().list(q='trashed=false').execute()["items"]
        # print('Contents of your drive are: ' + str(self.drive_file_list))

    def get_list_of_drives_files(self):
        return self.drive_file_list

    def build_drive_service(self, client_secret_file, api_name, api_version, *scopes):
        print(client_secret_file, api_name, api_version, scopes, sep="-")
        CLIENT_SECRET_FILE = client_secret_file
        API_SERVICE_NAME = api_name
        API_VERSION = api_version
        SCOPES = [scope for scope in scopes[0]]
        print(SCOPES)

        cred = None

        pickle_file = f"token_{API_SERVICE_NAME}_{API_VERSION}.pickle"

        if os.path.exists(pickle_file):
            with open(pickle_file, "rb") as token:
                cred = pickle.load(token)

        if not cred or not cred.valid:
            if cred and cred.expired and cred.refresh_token:
                cred.refresh(Request())
            else:
                flow = InstalledAppFlow.from_client_secrets_file(CLIENT_SECRET_FILE, SCOPES)
                cred = flow.run_local_server()

            with open(pickle_file, "wb") as token:
                pickle.dump(cred, token)

        try:
            service = build(API_SERVICE_NAME, API_VERSION, credentials=cred)
            print(API_SERVICE_NAME.capitalize(), "service created successfully.\n")
            return service
        except Exception as e:
            print("Unable to connect.")
            print(e)
            return None

    def get_drive(self):
        return self.drive_service

