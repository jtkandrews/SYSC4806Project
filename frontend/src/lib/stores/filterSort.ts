import { writable, derived } from 'svelte/store';
import { booksStore } from './books';
import type { Book } from '$lib/types';

export type SortOption = 'title-asc' | 'title-desc' | 'price-asc' | 'price-desc' | 'author-asc' | 'author-desc' | 'none';

export interface FilterState {
  sortBy: SortOption;
  genres: string[];
  minPrice: number;
  maxPrice: number;
  searchTerm: string;
}

const initialFilterState: FilterState = {
  sortBy: 'none',
  genres: [],
  minPrice: 0,
  maxPrice: 10000,
  searchTerm: ''
};

export const filterState = writable<FilterState>(initialFilterState);

// Derived store for filtered and sorted books
export const filteredSortedBooks = derived(
  [booksStore, filterState],
  ([$books, $filterState]) => {
    let result = [...$books];

    // Apply search filter
    if ($filterState.searchTerm.trim()) {
      const searchLower = $filterState.searchTerm.toLowerCase();
      result = result.filter(
        book =>
          book.title.toLowerCase().includes(searchLower) ||
          book.author.toLowerCase().includes(searchLower) ||
          book.description?.toLowerCase().includes(searchLower)
      );
    }

    // Apply genre filter
    if ($filterState.genres.length > 0) {
      result = result.filter(book => {
        if (!book.genre) return false;
        const bookGenres = book.genre.split(',').map(g => g.trim().toLowerCase());
        return $filterState.genres.some(selectedGenre =>
          bookGenres.includes(selectedGenre.toLowerCase())
        );
      });
    }

    // Apply price filter
    result = result.filter(
      book => book.price >= $filterState.minPrice && book.price <= $filterState.maxPrice
    );

    // Apply sorting
    switch ($filterState.sortBy) {
      case 'title-asc':
        result.sort((a, b) => a.title.localeCompare(b.title));
        break;
      case 'title-desc':
        result.sort((a, b) => b.title.localeCompare(a.title));
        break;
      case 'price-asc':
        result.sort((a, b) => a.price - b.price);
        break;
      case 'price-desc':
        result.sort((a, b) => b.price - a.price);
        break;
      case 'author-asc':
        result.sort((a, b) => a.author.localeCompare(b.author));
        break;
      case 'author-desc':
        result.sort((a, b) => b.author.localeCompare(a.author));
        break;
      case 'none':
      default:
        // Keep original order
        break;
    }

    return result;
  }
);

// Helper functions to update filter state
export function updateSortBy(sortOption: SortOption): void {
  filterState.update(state => ({ ...state, sortBy: sortOption }));
}

export function updateGenreFilter(genres: string[]): void {
  filterState.update(state => ({ ...state, genres }));
}

export function toggleGenre(genre: string): void {
  filterState.update(state => {
    const genres = state.genres.includes(genre)
      ? state.genres.filter(g => g !== genre)
      : [...state.genres, genre];
    return { ...state, genres };
  });
}

export function updatePriceRange(minPrice: number, maxPrice: number): void {
  filterState.update(state => ({ ...state, minPrice, maxPrice }));
}

export function updateSearchTerm(term: string): void {
  filterState.update(state => ({ ...state, searchTerm: term }));
}

export function resetFilters(): void {
  filterState.set(initialFilterState);
}
