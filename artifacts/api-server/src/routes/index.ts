import { Router, type IRouter } from "express";
import healthRouter from "./health";
import apkRouter from "./apk";
import analyticsRouter from "./analytics";

const router: IRouter = Router();

router.use(healthRouter);
router.use("/apk", apkRouter);
router.use("/analytics", analyticsRouter);

export default router;
