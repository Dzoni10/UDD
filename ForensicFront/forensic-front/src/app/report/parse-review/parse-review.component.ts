import { Component, OnInit } from '@angular/core';
import { ParsedDocument } from '../model/ParsedDocument.model';
import { ConfirmIndexDocument } from '../model/ConfirmIndexDocument.model';
import { ReportService } from '../report.service';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-parse-review',
  templateUrl: './parse-review.component.html',
  styleUrls: ['./parse-review.component.css']
})
export class ParseReviewComponent implements OnInit{

  serverFilename: string= '';
  isLoading: boolean = true;
  isProcessing: boolean = false;

  parsedDocument: ParsedDocument | null = null;


  editedDocument: ConfirmIndexDocument = {
    server_filename: '',
    forensician_name: '',
    organization: '',
    organization_address: '',
    malware_name: '',
    malware_description: '',
    threat_level: 'medium',
    hash_md5: '',
    hash_sha256: ''
  };



  threatLevels = ['low', 'medium', 'high', 'critical'];
  threatLevelsLabels: { [key: string]: string } = {
    'low': '🟢 Low',
    'medium': '🟡 Medium',
    'high': '🟠 High',
    'critical': '🔴 Critical'

}


  constructor(
    private parsePreviewService: ReportService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.loadParsedDocument();
  }


  loadParsedDocument(): void {
    this.route.queryParams.subscribe(params => {
      this.serverFilename = params['filename'];

      if (!this.serverFilename) {
        this.snackBar.open('❌ Filename not found!', 'Close', {
          duration: 5000,
          panelClass: ['snackbar-error']
        });
        this.router.navigate(['/documents/upload']);
        return;
      }

      this.parsePreviewService.parseDocument(this.serverFilename).subscribe({
        next: (document) => {
          this.parsedDocument = document;
          this.initializeEditedDocument();
          this.isLoading = false;

          this.snackBar.open(
            `✅ Document parsed! Details: ${(document.confidence_score * 100).toFixed(0)}%`,
            'Close',
            {
              duration: 3000,
              panelClass: ['snackbar-success']
            }
          );
        },
        error: (error) => {
          this.isLoading = false;

          const errorMessage = error.error?.message || 'Error during parse document!';
          this.snackBar.open(`❌ ${errorMessage}`, 'Close', {
            duration: 5000,
            panelClass: ['snackbar-error']
          });

          console.error('Parse error:', error);
          setTimeout(() => {
            this.router.navigate(['/documents/upload']);
          }, 2000);
        }
      });
    });
  }

  private initializeEditedDocument(): void {
    if (this.parsedDocument) {
      this.editedDocument = {
        server_filename: this.parsedDocument.server_filename,
        forensician_name: this.parsedDocument.forensician_name,
        organization: this.parsedDocument.organization,
        organization_address: this.parsedDocument.organization_address,
        malware_name: this.parsedDocument.malware_name,
        malware_description: this.parsedDocument.malware_description,
        threat_level: this.parsedDocument.threat_level,
        hash_md5: this.parsedDocument.hash_md5,
        hash_sha256: this.parsedDocument.hash_sha256
      };
    }
  }


  confirmAndIndex(): void {
    // Validacija
    if (!this.editedDocument.forensician_name.trim()) {
      this.snackBar.open('Forensician name is required!', 'Close', {
        duration: 3000,
        panelClass: ['snackbar-error']
      });
      return;
    }

    if (!this.editedDocument.organization.trim()) {
      this.snackBar.open('Organization is required!', 'Close', {
        duration: 3000,
        panelClass: ['snackbar-error']
      });
      return;
    }

    if (!this.editedDocument.malware_name.trim()) {
      this.snackBar.open('Name of malware is required','Close', {
        duration: 3000,
        panelClass: ['snackbar-error']
      });
      return;
    }

    this.isProcessing = true;

    this.parsePreviewService.confirmAndIndex(this.editedDocument).subscribe({
      next: (response) => {
        this.isProcessing = false;

        this.snackBar.open(
          `✅ Document is successfully indexed!`,
          'Close',
          {
            duration: 5000,
            panelClass: ['snackbar-success']
          }
        );

        setTimeout(() => {
          this.router.navigate(['search']);
        }, 2000);
      },
      error: (error) => {
        this.isProcessing = false;

        const errorMessage = error.error?.message || 'Error druing indexing!';
        this.snackBar.open(`❌ ${errorMessage}`, 'Close', {
          duration: 5000,
          panelClass: ['snackbar-error']
        });

        console.error('Index error:', error);
      }
    });
  }

  
  cancelAndDelete(): void {
    const confirmDelete = window.confirm(
      '⚠️ Are you sure?\n\nDocument will be deleted'
    );

    if (!confirmDelete) return;

    this.isProcessing = true;

    this.parsePreviewService.cancelAndDelete(this.serverFilename).subscribe({
      next: (response) => {
        this.isProcessing = false;

        this.snackBar.open(
          `⚠️ Document is deleted`,
          'Close',
          {
            duration: 3000,
            panelClass: ['snackbar-warning']
          }
        );

        setTimeout(() => {
          this.router.navigate(['/documents/upload']);
        }, 1500);
      },
      error: (error) => {
        this.isProcessing = false;

        const errorMessage = error.error?.message || 'Error during deleting!';
        this.snackBar.open(`❌ ${errorMessage}`, 'Close', {
          duration: 5000,
          panelClass: ['snackbar-error']
        });

        console.error('Delete error:', error);
      }
    });
  }

  getThreatLevelLabel(level: string): string {
    return this.threatLevelsLabels[level] || level;
  }

  getThreatLevelColor(level: string): string {
    switch (level) {
      case 'low': return '#4CAF50';
      case 'medium': return '#FF9800';
      case 'high': return '#FF5722';
      case 'critical': return '#F44336';
      default: return '#9E9E9E';
    }
  }

  
  isFieldModified(fieldName: keyof ConfirmIndexDocument): boolean {
    if (!this.parsedDocument) return false;
    
    const originalValue = this.parsedDocument[fieldName as keyof ParsedDocument];
    const editedValue = this.editedDocument[fieldName];
    
    return originalValue !== editedValue;
  }

  getModifiedFieldsCount(): number {
    let count = 0;
    const keys = Object.keys(this.editedDocument) as Array<keyof ConfirmIndexDocument>;
    
    keys.forEach(key => {
      if (this.isFieldModified(key)) {
        count++;
      }
    });
    
    return count;
  }


  
}




