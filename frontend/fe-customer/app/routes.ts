import { type RouteConfig, index, route } from "@react-router/dev/routes";

export default [
    index("routes/home.tsx"),
    route("login", "routes/login.tsx"),
    route("register", "routes/register.tsx"),
    route("dashboard", "routes/dashboard.tsx"),
    route("profile", "routes/profile.tsx"),
    route("transfer", "routes/transfer.tsx"),
    route("transfer/internal", "routes/transferInternal.tsx"),
    route("transfer/interbank", "routes/transferInterbank.tsx"),
    route("transfer/fast247", "routes/transferFast.tsx"),
    route("accounts/list", "routes/accountList.tsx"),
    route("transactions", "routes/transactions.tsx"),
    // route("accounts/detail/:accountNumber", "routes/accountDetail.tsx"),
    route("notifications", "routes/notification.tsx"),
    
    // Savings routes
    route("saving/books", "routes/savingsBooks.tsx"),
    route("saving/open", "routes/savingsOpen.tsx"),
    route("saving/close", "routes/savingsClose.tsx"),
    
    // Loan routes
    route("loan/my-loans", "routes/loanMyLoans.tsx"),
    route("loan/pay", "routes/loanPay.tsx"),
    route("loan/apply", "routes/loanApply.tsx")

] satisfies RouteConfig;
