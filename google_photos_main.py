import kivy
from kivy.lang import Builder
from kivy.uix.gridlayout import GridLayout
from kivy.uix.popup import Popup
from kivy.app import App
from apiclient.http import MediaIoBaseDownload
import io
from googleapiclient.discovery import build
from google_auth_oauthlib.flow import InstalledAppFlow
from google.auth.transport.requests import Request
import pickle

import sys
sys.path.append('GoogleNetworking')
from googleapiclient.http import MediaFileUpload
from googleapiclient import errors

import logging
from google_drive_handler import GoogleDriveHandler
import os
import pprint

# https://github.com/kewok/Kivy_GoogleDrive

# CLIENT_ID = ['1hGB5zv7wW3NznUDppM1BvHAMnBYbzXyU']

Builder.load_string('''
<MakeFolderScreen>:
    orientation: "lr-tb"
    cols: 1
    Button: 
        text: 'Authenticate'
        font_size: 50
        on_press: root.authenticate()
    Button: 
        text: 'Upload'
        font_size: 50
        on_press: root.upload('Test_File.txt')
    Button:
        text: 'Download and display'
        font_size: 50
        # on_press: root.download('Test_File.txt')
        on_press: root.download(['1hGB5zv7wW3NznUDppM1BvHAMnBYbzXyU'])
''')		

class MakeFolderScreen(GridLayout):
    def authenticate(self):
        print('current working directory: ' + os.getcwd())
        google_drive = GoogleDriveHandler(sys.argv)
        print('authentication successful and drive object is created.')
        return google_drive.get_drive()
        
    def upload(self, FILENAME):
        print('Try to upload\n')
        if FILENAME == 'Test_File.txt':
            return
        media_body = MediaFileUpload(FILENAME, mimetype='text/plain', resumable=True)
        body = {'title': FILENAME, 'description': 'A test document', 'mimeType': 'text/plain'}
        try:
            self.new_file = self.drive_handler.drive_service.files().insert(body=body, media_body=media_body).execute()
            pprint.pprint(self.new_file)
        except (errors.HttpError, error):
            print('An error has occured: %s' % error)    

    #  else:
        #     print('The file does not exist in your drive')
        #     popup = Popup(title='FILE NOT AVAILABLE', content=Label(text='File does not exist\n in your google drive',font_size=50), size_hint=(None, None), size=(500, 500))
        #     popup.open()
    # Download folders with files
    def download(self, folder_ids):

        # Download files
        def downloadfiles(dowid, dfilespath, folder=None):
            request = service.files().get_media(fileId=dowid)
            fh = io.BytesIO()
            downloader = MediaIoBaseDownload(fh, request)
            done = False
            while done is False:
                status, done = downloader.next_chunk()
                print("Download %d%%." % int(status.progress() * 100))
            if folder:
                with io.open(folder + "/" + dfilespath, "wb") as f:
                    fh.seek(0)
                    f.write(fh.read())
            else:
                with io.open(dfilespath, "wb") as f:
                    fh.seek(0)
                    f.write(fh.read())


        # List files in folder until all files are found
        def listfolders(filid, des):
            page_token = None
            while True:
                results = (
                    service.files()
                    .list(
                        pageSize=1000,
                        q="'" + filid + "'" + " in parents",
                        fields="nextPageToken, files(id, name, mimeType)",
                    )
                    .execute()
                )
                page_token = results.get("nextPageToken", None)
                if page_token is None:
                    folder = results.get("files", [])
                    for item in folder:
                        if str(item["mimeType"]) == str("application/vnd.google-apps.folder"):
                            if not os.path.isdir(des + "/" + item["name"]):
                                os.mkdir(path=des + "/" + item["name"])
                            listfolders(item["id"], des + "/" + item["name"])
                        else:
                            downloadfiles(item["id"], item["name"], des)
                            print(item["name"])
                break
            return folder

        service = self.authenticate()
        print(f'service: {service}')
        for folder_id in folder_ids:
            
            print(f'folder_ids: {folder_ids} && folder_id: {folder_id}')

            folder = service.files().get(fileId=folder_id).execute()
            folder_name = folder.get("name")
            page_token = None
            while True:
                results = (
                    service.files()
                    .list(
                        q=f"'{folder_id}' in parents",
                        spaces="drive",
                        fields="nextPageToken, files(id, name, mimeType)",
                    )
                    .execute()
                )
                page_token = results.get("nextPageToken", None)
                if page_token is None:
                    items = results.get("files", [])
                    if not items:
                        # download files
                        downloadfiles(folder_id, folder_name) 
                        print(folder_name)
                    else:
                        # download folders
                        print(f"Start downloading folder '{folder_name}'.")
                        for item in items:
                            if item["mimeType"] == "application/vnd.google-apps.folder":
                                if not os.path.isdir(folder_name):
                                    os.mkdir(folder_name)
                                bfolderpath = os.path.join(os.getcwd(), folder_name)
                                if not os.path.isdir(
                                    os.path.join(bfolderpath, item["name"])
                                ):
                                    os.mkdir(os.path.join(bfolderpath, item["name"]))

                                folderpath = os.path.join(bfolderpath, item["name"])
                                listfolders(item["id"], folderpath)
                            else:
                                if not os.path.isdir(folder_name):
                                    os.mkdir(folder_name)
                                bfolderpath = os.path.join(os.getcwd(), folder_name)

                                filepath = os.path.join(bfolderpath, item["name"])
                                downloadfiles(item["id"], filepath)
                                print(item["name"])
                break

class DriveTest(App):
    def build(self):
        return MakeFolderScreen() 

    def on_pause(self):
        # Save data?
        return True

    def on_resume(self):
        # Here you can check if any data needs replacing (usually nothing)
        return True

if __name__ == '__main__':
    DriveTest().run()