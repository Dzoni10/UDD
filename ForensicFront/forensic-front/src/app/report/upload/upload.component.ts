import { Component, OnInit } from '@angular/core';
import { ReportService } from '../report.service';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DocumentUploadResponse } from '../model/DocumentploadResponse.model';
import { HttpProgressEvent,HttpEventType } from '@angular/common/http';

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.css']
})
export class UploadComponent implements OnInit {


  selectedFile: File | null = null;
  fileName: string = '';
  fileSize: number = 0;
  uploadProgress: number = 0;
  isUploading: boolean = false;
  isDragOver: boolean = false;

  
  uploadedDocument: DocumentUploadResponse | null = null;

  // Validacijske konstante
  MAX_FILE_SIZE_MB = 50;
  ALLOWED_FILE_TYPE = 'application/pdf';

  constructor(
    private documentService: ReportService,
    private router: Router,
    private snackBar: MatSnackBar){}

    ngOnInit(): void {}

    
  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.handleFileSelection(files[0]);
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.handleFileSelection(input.files[0]);
    }
  }


  private handleFileSelection(file: File): void {
    // Validacija file type-a
    if (file.type !== this.ALLOWED_FILE_TYPE) {
      this.snackBar.open('Only pdf files are allowed!', 'Close', {
        duration: 5000,
        panelClass: ['snackbar-error']
      });
      return;
    }

    // Validacija file size-a
    const fileSizeMB = file.size / (1024 * 1024);
    if (fileSizeMB > this.MAX_FILE_SIZE_MB) {
      this.snackBar.open(
        ` Faile is too big ${this.MAX_FILE_SIZE_MB}MB.`,
        'Close',
        {
          duration: 5000,
          panelClass: ['snackbar-error']
        }
      );
      return;
    }
    this.selectedFile = file;
    this.fileName = file.name;
    this.fileSize = file.size;

    this.snackBar.open(`File "${file.name}" is ready for upload!`, 'Close', {
      duration: 3000,
      panelClass: ['snackbar-success']
    });
  }

 
  uploadFile(): void {
    if (!this.selectedFile) {
      this.snackBar.open('Please choose file!', 'Close', {
        duration: 3000,
        panelClass: ['snackbar-error']
      });
      return;
    }

    this.isUploading = true;
    this.uploadProgress = 0;


    this.documentService.uploadDocument(this.selectedFile).subscribe({
      next: (event) => {
        if (event.type === HttpEventType.UploadProgress) {
          const progressEvent = event as HttpProgressEvent;
          if (progressEvent.total) {
            this.uploadProgress = Math.round((progressEvent.loaded / progressEvent.total) * 100);
          }
        } else if (event.type === HttpEventType.Response) {
          const response = event.body as DocumentUploadResponse;
          this.uploadedDocument = response;
          this.isUploading = false;

          this.snackBar.open(
            `File successfully loaded! Filename: ${response.server_filename}`,
            'Close',
            {
              duration: 5000,
              panelClass: ['snackbar-success']
            }
          );

          
          setTimeout(() => {
            this.router.navigate(['parse'], {
              queryParams: { filename: response.server_filename }
            });
          }, 2000);
        }
      },
      error: (error) => {
        this.isUploading = false;
        this.uploadProgress = 0;

        const errorMessage = error.error?.message || 'Error during file upload!';
        this.snackBar.open(` ${errorMessage}`, 'Close', {
          duration: 5000,
          panelClass: ['snackbar-error']
        });

        console.error('Upload error:', error);
      }
    });
  }

  
  cancelUpload(): void {
    this.selectedFile = null;
    this.fileName = '';
    this.fileSize = 0;
    this.uploadProgress = 0;

    this.snackBar.open('Upload canceled', 'Close', {
      duration: 3000,
      panelClass: ['snackbar-warning']
    });
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  }

  isFileSelected(): boolean {
    return this.selectedFile !== null;
  }
}
