export type Book = {
    isbn: string;
    title: string;
    author: string;
    publisher?: string;
    genre?: string;
    description?: string;
    price: number;      // dollars
    inventory: number;
    imageUrl: string;
};
