import { SearchResult } from "./SearchResult.model";

export interface SearchPageResponse {
  content: SearchResult[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}
