export type Book = {
    isbn: string;
    title: string;
    author: string;
    publisher?: string;
    description?: string;
    price: number;      // dollars
    inventory: number;
};
