import { Component, OnInit } from '@angular/core';
import { ReportService } from '../report.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SearchResult } from '../model/SearchResult.model';
import { BasicSearchRequest } from '../model/BasicSearchRequest.model';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {


  
  searchRequest: BasicSearchRequest = {
    forensician_name: '',
    organization: '',
    malware_name: '',
    hash_value: '',
    threat_level: '',
    search_text: '',
    is_knn: false
  };
  
  results: SearchResult[] = [];
  isSearching: boolean = false;
  totalResults: number = 0;
  currentPage: number = 0;
  pageSize: number = 10;
  totalPages: number = 0;
  
  activeTab: 'basic' | 'advanced' | 'knn' | 'phrase' | 'fulltext' = 'basic';
  threatLevels = ['', 'low', 'medium', 'high', 'critical'];
  threatLevelLabels: { [key: string]: string } = {
    '': 'Sve',
    'low': '🟢 Low',
    'medium': '🟡 Medium',
    'high': '🟠 High',
    'critical': '🔴 Critical'
  };

  
  advancedExpressions: string[] = ['', ''];
  advancedOperators: string[] = ['AND'];

  // KNN search
  knnQuery: string = '';

  // Phrase search
  phraseQuery: string = '';

  // Fulltext search
  fulltextQuery: string = '';

  
  constructor(
    private searchService: ReportService,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {

  }

  //basic search

  performBasicSearch(page: number = 0): void {
    // Validacija
    if (!this.isAnyFieldFilled(this.searchRequest)) {
      this.snackBar.open('Input data at least one field for search!', 'Close', {
        duration: 3000,
        panelClass: ['snackbar-error']
      });
      return;
    }

    this.isSearching = true;
    this.currentPage = page;

    this.searchService.basicSearch(this.searchRequest, page, this.pageSize).subscribe({
      next: (response) => {
        this.results = response.content;
        this.totalResults = response.totalElements;
        this.totalPages = response.totalPages;
        this.isSearching = false;

        this.snackBar.open(
          `Found ${this.totalResults} results!`,
          'Close',
          { duration: 3000, panelClass: ['snackbar-success'] }
        );
      },
      error: (error) => {
        this.isSearching = false;
        const msg = error.error?.message || 'Error during search!';
        this.snackBar.open(`${msg}`, 'Close', {
          duration: 5000,
          panelClass: ['snackbar-error']
        });
      }
    });
  }

 //Advanced search
 
  performAdvancedSearch(page: number = 0): void {
    if (this.advancedExpressions.some(e => !e || e.trim() === '')) {
      this.snackBar.open('Input all expressions!', 'Close', {
        duration: 3000,
        panelClass: ['snackbar-error']
      });
      return;
    }

    this.isSearching = true;
    const request = {
      expressions: this.advancedExpressions,
      operators: this.advancedOperators
    };

    this.searchService.advancedSearch(request, page, this.pageSize).subscribe({
      next: (response) => {
        this.results = response.content;
        this.totalResults = response.totalElements;
        this.totalPages = response.totalPages;
        this.isSearching = false;

        this.snackBar.open(
          `Found ${this.totalResults} results!`,
          'Close',
          { duration: 3000, panelClass: ['snackbar-success'] }
        );
      },
      error: (error) => {
        this.isSearching = false;
        const msg = error.error?.message || 'Error during search';
        this.snackBar.open(`${msg}`, 'Close', {
          duration: 5000,
          panelClass: ['snackbar-error']
        });
      }
    });
  }


  //knn search

  performKnnSearch(page: number = 0): void {
    if (!this.knnQuery.trim()) {
      this.snackBar.open('Input text for search!', 'Close', {
        duration: 3000,
        panelClass: ['snackbar-error']
      });
      return;
    }

    this.isSearching = true;

    this.searchService.knnSearch(this.knnQuery, page, this.pageSize).subscribe({
      next: (response) => {
        this.results = response.content;
        this.totalResults = response.totalElements;
        this.totalPages = response.totalPages;
        this.isSearching = false;

        this.snackBar.open(
          `Found ${this.totalResults} similar results`,
          'Close',
          { duration: 3000, panelClass: ['snackbar-success'] }
        );
      },
      error: (error) => {
        this.isSearching = false;
        const msg = error.error?.message || 'Error during KNN search!';
        this.snackBar.open(`${msg}`, 'Close', {
          duration: 5000,
          panelClass: ['snackbar-error']
        });
      }
    });
  }

  //Phrase search

  performPhraseSearch(page: number = 0): void {
    if (!this.phraseQuery.trim()) {
      this.snackBar.open('Input phrase for search', 'Close', {
        duration: 3000,
        panelClass: ['snackbar-error']
      });
      return;
    }
    this.isSearching = true;
    this.searchService.phraseSearch(this.phraseQuery, page, this.pageSize).subscribe({
      next: (response) => {
        this.results = response.content;
        this.totalResults = response.totalElements;
        this.totalPages = response.totalPages;
        this.isSearching = false;

        this.snackBar.open(
          `Found ${this.totalResults} results for phrase`,
          'Close',
          { duration: 3000, panelClass: ['snackbar-success'] }
        );
      },
      error: (error) => {
        this.isSearching = false;
        const msg = error.error?.message || 'Error during phrase search';
        this.snackBar.open(`${msg}`, 'Close', {
          duration: 5000,
          panelClass: ['snackbar-error']
        });
      }
    });
  }

  performFulltextSearch(page: number = 0): void {
    if (!this.fulltextQuery.trim()) {
      this.snackBar.open('Input text for search', 'Close', {
        duration: 3000,
        panelClass: ['snackbar-error']
      });
      return;
    }
    this.isSearching = true;
    this.searchService.fullTextSearch(this.fulltextQuery, page, this.pageSize).subscribe({
      next: (response) => {
        this.results = response.content;
        this.totalResults = response.totalElements;
        this.totalPages = response.totalPages;
        this.isSearching = false;

        this.snackBar.open(
          `Found ${this.totalResults} results`,
          'Close',
          { duration: 3000, panelClass: ['snackbar-success'] }
        );
      },
      error: (error) => {
        this.isSearching = false;
        const msg = error.error?.message || 'Error during full-text search!';
        this.snackBar.open(`${msg}`, 'Close', {
          duration: 5000,
          panelClass: ['snackbar-error']
        });
      }
    });
  }

  addAdvancedExpression(): void {
    this.advancedExpressions.push('');
    this.advancedOperators.push('AND');
  }

  removeAdvancedExpression(index: number): void {
    if (this.advancedExpressions.length > 2) {
      this.advancedExpressions.splice(index, 1);
      this.advancedOperators.splice(index, 1);
    }
  }

  

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      switch (this.activeTab) {
        case 'basic':
          this.performBasicSearch(page);
          break;
        case 'advanced':
          this.performAdvancedSearch(page);
          break;
        case 'knn':
          this.performKnnSearch(page);
          break;
        case 'phrase':
          this.performPhraseSearch(page);
          break;
        case 'fulltext':
          this.performFulltextSearch(page);
          break;
      }
    }
  }

  clearForm(): void {
    this.searchRequest = {
      forensician_name: '',
      organization: '',
      malware_name: '',
      hash_value: '',
      threat_level: '',
      search_text: '',
      is_knn: false
    };
    this.results = [];
    this.totalResults = 0;
  }

  private isAnyFieldFilled(obj: BasicSearchRequest): boolean {
    return Object.values(obj).some(val => val !== '' && val !== false);
  }

  getThreatLevelLabel(level: string): string {
    return this.threatLevelLabels[level] || level;
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

  highlightText(text: string, term: string): string {
    if (!term) return text;
    const regex = new RegExp(`(${term})`, 'gi');
    return text.replace(regex, '<strong class="highlight">$1</strong>');
  }
}

