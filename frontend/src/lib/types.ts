export type Book = {
    isbn: string;
    title: string;
    author: string;
    publisher?: string;
    genre?: string;
    price: number;      // dollars
    inventory: number;
    imageUrl: string;
};
